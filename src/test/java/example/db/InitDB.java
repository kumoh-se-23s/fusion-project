package example.db;

import config.AppConfig;
import config.PooledDataSource;
import constant.FundingState;
import model.domain.*;
import repository.BaseRepositoryImpl;
import repository.Filter;
import repository.Order;
import repository.Sort;
import repository.manager.query_dsl.EntityMetadata;
import repository.manager.schema.SchemaGenerator;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 테스트 DB의 테이블 생성(create), 기본 데이터 삽입(init), 테이블 삭제(drop), 기본 데이터 조회(load)를 담당하는 클래스
 * 기본 데이터는 읽기 전용이다. 생성, 수정 테스트는 필요한 데이터를 직접 만들어서 사용한다.
 */
public class InitDB {
    private static final List<Class<?>> ENTITIES = List.of(
            AppUser.class, UserRole.class, UserRoleRelation.class, LocalAccount.class, Category.class,
            Funding.class, DenyFunding.class, FundingCategory.class, FundingLike.class,
            Reward.class, Donate.class, DonateCancel.class, Review.class, ReviewLike.class);

    public final AppConfig config = new AppConfig();

    public AppUser maker;
    public AppUser deliveredUser;     //completedFunding에 후원, 배송 완료
    public AppUser undeliveredUser;   //completedFunding에 후원, 배송 전
    public AppUser newUser;           //후원 내역 없음

    public Category food;
    public Category tech;

    public Funding completedFunding;  //카테고리 food, tech
    public Funding pendingFunding;    //카테고리 tech
    public Funding noCategoryFunding; //JUST_USER_INPUT

    public Reward completedReward;
    public Reward openReward;         //남은 수량 5
    public Reward soldOutReward;      //남은 수량 0

    private InitDB() {
    }

    //DB 초기화. 처음 한 번, 또는 쌓인 테스트 데이터를 비우고 싶을 때 직접 실행한다
    public static void main(String[] args) {
        drop();
        create();
        init();
    }

    //이미 삽입된 기본 데이터를 불러온다. DB를 변경하지 않는다
    public static InitDB load() {
        InitDB db = new InitDB();

        db.maker = first(db.config.getUserRepository(), "email", "maker@test.com");
        db.deliveredUser = first(db.config.getUserRepository(), "email", "delivered@test.com");
        db.undeliveredUser = first(db.config.getUserRepository(), "email", "undelivered@test.com");
        db.newUser = first(db.config.getUserRepository(), "email", "new@test.com");

        db.food = first(db.config.getCategoryRepository(), "name", "food");
        db.tech = first(db.config.getCategoryRepository(), "name", "tech");

        db.completedFunding = first(db.config.getFundingRepository(), "title", "completed");
        db.pendingFunding = first(db.config.getFundingRepository(), "title", "pending");
        db.noCategoryFunding = first(db.config.getFundingRepository(), "title", "noCategory");

        db.completedReward = first(db.config.getRewardRepository(), "name", "completedReward");
        db.openReward = first(db.config.getRewardRepository(), "name", "openReward");
        db.soldOutReward = first(db.config.getRewardRepository(), "name", "soldOutReward");
        return db;
    }

    //테스트가 같은 값으로 데이터를 만들어도 먼저 삽입된 기본 데이터를 찾도록 PK 순으로 조회한다
    private static <T extends BaseDomain> T first(BaseRepositoryImpl<T, Long> repository, String field, Object value) {
        List<T> result = repository.find(Filter.builder()
                .conditions(Map.of(field, value))
                .sort(Sort.by(Order.asc("pk")))
                .limit(1)
                .build());
        if (result.isEmpty()) {
            throw new IllegalStateException("Seed data not found (" + field + "=" + value + "). Run InitDB.main first");
        }
        return result.get(0);
    }

    //테이블 생성
    public static void create() {
        new SchemaGenerator().execute(SchemaGenerator.Mode.CREATE_IF_MISSING, "model.domain");
    }

    //기본 데이터 삽입
    public static InitDB init() {
        InitDB db = new InitDB();
        db.insert();
        return db;
    }

    //모든 테이블 삭제
    public static void drop() {
        try (Connection connection = PooledDataSource.getDataSource().getConnection();
             Statement statement = connection.createStatement()) {
            //FK가 걸린 테이블도 순서와 상관없이 삭제할 수 있도록 검사를 잠시 끔
            statement.execute("SET FOREIGN_KEY_CHECKS = 0");
            try {
                for (Class<?> entity : ENTITIES) {
                    statement.execute("DROP TABLE IF EXISTS " + EntityMetadata.of(entity).getTableName());
                }
            } finally {
                statement.execute("SET FOREIGN_KEY_CHECKS = 1");
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to drop test tables", e);
        }
    }

    //기본 데이터를 리포지토리로 삽입
    private void insert() {
        LocalDateTime now = LocalDateTime.now();

        maker = createUser("maker");
        deliveredUser = createUser("delivered");
        undeliveredUser = createUser("undelivered");
        newUser = createUser("new");

        food = config.getCategoryRepository().create(Category.builder().name("food").build());
        tech = config.getCategoryRepository().create(Category.builder().name("tech").build());

        completedFunding = createFunding("completed", FundingState.COMPLETED, now);
        pendingFunding = createFunding("pending", FundingState.PENDING_APPROVAL, now);
        noCategoryFunding = createFunding("noCategory", FundingState.JUST_USER_INPUT, now);

        bindCategory(completedFunding, food);
        bindCategory(completedFunding, tech);
        bindCategory(pendingFunding, tech);

        completedReward = createReward("completedReward", completedFunding, 5L, now);
        openReward = createReward("openReward", pendingFunding, 5L, now);
        soldOutReward = createReward("soldOutReward", pendingFunding, 0L, now);

        createDonate(deliveredUser, completedReward, true);
        createDonate(undeliveredUser, completedReward, false);
    }

    private AppUser createUser(String name) {
        return config.getUserRepository().create(AppUser.builder()
                .name(name)
                .email(name + "@test.com")
                .postal("12345")
                .address(name + " address")
                .phone("010-0000-0000")
                .build());
    }

    private Funding createFunding(String title, FundingState state, LocalDateTime now) {
        return config.getFundingRepository().create(Funding.builder()
                .maker(maker)
                .title(title)
                .state(state)
                .purposeAmount(new BigDecimal("10000"))
                .endDate(now.plusDays(30))
                .build());
    }

    private void bindCategory(Funding funding, Category category) {
        config.getFundingCategoryRepository().create(FundingCategory.builder()
                .funding(funding)
                .category(category)
                .build());
    }

    private Reward createReward(String name, Funding funding, Long remainQuantity, LocalDateTime now) {
        return config.getRewardRepository().create(Reward.builder()
                .funding(funding)
                .category(food)
                .name(name)
                .requiredAmount(new BigDecimal("1000"))
                .maxQuantity(5L)
                .remainQuantity(remainQuantity)
                .provideDate(now.plusDays(60))
                .build());
    }

    private void createDonate(AppUser user, Reward reward, boolean delivered) {
        config.getDonateRepository().create(Donate.builder()
                .user(user)
                .reward(reward)
                .amount(new BigDecimal("1000"))
                .quantity(1L)
                .state(delivered)
                .build());
    }
}
