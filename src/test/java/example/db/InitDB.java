package example.db;

import config.AppConfig;
import config.PooledDataSource;
import constant.FundingState;
import model.domain.*;
import repository.manager.query_dsl.EntityMetadata;
import repository.manager.schema.SchemaGenerator;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 테스트 DB의 테이블 생성(create), 기본 데이터 삽입(init), 테이블 삭제(drop)를 담당하는 클래스
 * 생성, 수정 테스트는 기본 데이터를 바꾸므로, 다시 실행하려면 InitDB.main으로 초기화한다.
 * 필드의 pk는 초기화 직후 insert() 순서대로 매겨지는 값이므로, 삽입 순서를 바꾸면 pk도 함께 바꾼다.
 */
public class InitDB {
    private static final List<Class<?>> ENTITIES = List.of(
            AppUser.class, UserRole.class, UserRoleRelation.class, LocalAccount.class, Category.class,
            Funding.class, DenyFunding.class, FundingCategory.class, FundingLike.class,
            Reward.class, Donate.class, DonateCancel.class, Review.class, ReviewLike.class);

    public final AppConfig config = new AppConfig();

    private final LocalDateTime now = LocalDateTime.now();

    public AppUser maker1 = AppUser.builder()
            .pk(1L)
            .name("메이커1")
            .address("메이커1 주소")
            .email("메이커1 이메일")
            .phone("메이커1 전화번호")
            .postal("메이커1 우편번호")
            .build();
    public AppUser maker2 = AppUser.builder()
            .pk(2L)
            .name("메이커2")
            .address("메이커2 주소")
            .email("메이커2 이메일")
            .phone("메이커2 전화번호")
            .postal("메이커2 우편번호")
            .build();

    public AppUser donateUser1 = AppUser.builder()
            .pk(3L)
            .name("후원자1")
            .address("후원자1 주소")
            .email("후원자1 이메일")
            .phone("후원자1 전화번호")
            .postal("후원자1 우편번호")
            .build();
    public AppUser donateUser2 = AppUser.builder()
            .pk(4L)
            .name("후원자2")
            .address("후원자2 주소")
            .email("후원자2 이메일")
            .phone("후원자2 전화번호")
            .postal("후원자2 우편번호")
            .build();
    //후원 내역 없음
    public AppUser newUser = AppUser.builder()
            .pk(5L)
            .name("신규 사용자")
            .address("신규 사용자 주소")
            .email("신규 사용자 이메일")
            .phone("신규 사용자 전화번호")
            .postal("신규 사용자 우편번호")
            .build();

    public Category food = Category.builder()
            .pk(1L)
            .name("food")
            .description("음식")
            .build();

    public Category tech = Category.builder()
            .pk(2L)
            .name("tech")
            .description("기술")
            .build();
    public Category furniture = Category.builder()
            .pk(3L)
            .name("furniture")
            .description("가구").build();
    public Category travel = Category.builder()
            .pk(4L)
            .name("travel")
            .description("여행")
            .build();

    public Funding maker1PendingFunding = Funding.builder()
            .pk(1L)
            .maker(maker1)
            .title("메이커1 승인 대기 펀딩")
            .text("메이커1 승인 대기 펀딩 소개")
            .state(FundingState.PENDING_APPROVAL)
            .purposeAmount(new BigDecimal("100000"))
            .endDate(now.plusDays(30))
            .build();
    public Funding maker2PendingFunding = Funding.builder()
            .pk(2L)
            .maker(maker2)
            .title("메이커2 승인 대기 펀딩")
            .text("메이커2 승인 대기 펀딩 소개")
            .state(FundingState.PENDING_APPROVAL)
            .purposeAmount(new BigDecimal("100000"))
            .endDate(now.plusDays(30))
            .build();
    //후원 2건
    public Funding maker1ApproveFunding = Funding.builder()
            .pk(3L)
            .maker(maker1)
            .title("메이커1 승인 펀딩")
            .text("메이커1 승인 펀딩 소개")
            .state(FundingState.APPROVED)
            .purposeAmount(new BigDecimal("100000"))
            .donateAmount(new BigDecimal("3000"))
            .endDate(now.plusDays(30))
            .build();
    public Funding maker2ApproveFunding = Funding.builder()
            .pk(4L)
            .maker(maker2)
            .title("메이커2 승인 펀딩")
            .text("메이커2 승인 펀딩 소개")
            .state(FundingState.APPROVED)
            .purposeAmount(new BigDecimal("100000"))
            .endDate(now.plusDays(30))
            .build();
    //후원 2건
    public Funding maker1CompleteFunding = Funding.builder()
            .pk(5L)
            .maker(maker1)
            .title("메이커1 완료 펀딩")
            .text("메이커1 완료 펀딩 소개")
            .state(FundingState.COMPLETED)
            .purposeAmount(new BigDecimal("100000"))
            .donateAmount(new BigDecimal("3000"))
            .endDate(now.plusDays(30))
            .build();

    public Reward maker1PendingReward1 = Reward.builder()
            .pk(1L)
            .funding(maker1PendingFunding)
            .category(food)
            .name("메이커1 승인 대기 리워드1")
            .text("메이커1 승인 대기 리워드1 소개")
            .requiredAmount(new BigDecimal("1000"))
            .maxQuantity(10L)
            .remainQuantity(10L)
            .provideDate(now.plusDays(60))
            .build();
    public Reward maker1PendingReward2 = Reward.builder()
            .pk(2L)
            .funding(maker1PendingFunding)
            .category(food)
            .name("메이커1 승인 대기 리워드2")
            .text("메이커1 승인 대기 리워드2 소개")
            .requiredAmount(new BigDecimal("2000"))
            .maxQuantity(10L)
            .remainQuantity(10L)
            .provideDate(now.plusDays(60))
            .build();

    public Reward maker2PendingReward1 = Reward.builder()
            .pk(3L)
            .funding(maker2PendingFunding)
            .category(tech)
            .name("메이커2 승인 대기 리워드1")
            .text("메이커2 승인 대기 리워드1 소개")
            .requiredAmount(new BigDecimal("1000"))
            .maxQuantity(10L)
            .remainQuantity(10L)
            .provideDate(now.plusDays(60))
            .build();
    public Reward maker2PendingReward2 = Reward.builder()
            .pk(4L)
            .funding(maker2PendingFunding)
            .category(tech)
            .name("메이커2 승인 대기 리워드2")
            .text("메이커2 승인 대기 리워드2 소개")
            .requiredAmount(new BigDecimal("2000"))
            .maxQuantity(10L)
            .remainQuantity(10L)
            .provideDate(now.plusDays(60))
            .build();

    //후원 1건
    public Reward maker1ApproveReward1 = Reward.builder()
            .pk(5L)
            .funding(maker1ApproveFunding)
            .category(furniture)
            .name("메이커1 승인 리워드1")
            .text("메이커1 승인 리워드1 소개")
            .requiredAmount(new BigDecimal("1000"))
            .maxQuantity(10L)
            .remainQuantity(9L)
            .provideDate(now.plusDays(60))
            .build();
    //후원 1건
    public Reward maker1ApproveReward2 = Reward.builder()
            .pk(6L)
            .funding(maker1ApproveFunding)
            .category(furniture)
            .name("메이커1 승인 리워드2")
            .text("메이커1 승인 리워드2 소개")
            .requiredAmount(new BigDecimal("2000"))
            .maxQuantity(10L)
            .remainQuantity(9L)
            .provideDate(now.plusDays(60))
            .build();

    public Reward maker2ApproveReward1 = Reward.builder()
            .pk(7L)
            .funding(maker2ApproveFunding)
            .category(tech)
            .name("메이커2 승인 리워드1")
            .text("메이커2 승인 리워드1 소개")
            .requiredAmount(new BigDecimal("1000"))
            .maxQuantity(10L)
            .remainQuantity(10L)
            .provideDate(now.plusDays(60))
            .build();
    public Reward maker2ApproveReward2 = Reward.builder()
            .pk(8L)
            .funding(maker2ApproveFunding)
            .category(tech)
            .name("메이커2 승인 리워드2")
            .text("메이커2 승인 리워드2 소개")
            .requiredAmount(new BigDecimal("2000"))
            .maxQuantity(10L)
            .remainQuantity(10L)
            .provideDate(now.plusDays(60))
            .build();

    //후원 1건, 배송 완료
    public Reward maker1CompleteReward1 = Reward.builder()
            .pk(9L)
            .funding(maker1CompleteFunding)
            .category(travel)
            .name("메이커1 완료 리워드1")
            .text("메이커1 완료 리워드1 소개")
            .requiredAmount(new BigDecimal("1000"))
            .maxQuantity(10L)
            .remainQuantity(9L)
            .provideDate(now.plusDays(60))
            .build();
    //후원 1건, 배송 전
    public Reward maker1CompleteReward2 = Reward.builder()
            .pk(10L)
            .funding(maker1CompleteFunding)
            .category(travel)
            .name("메이커1 완료 리워드2")
            .text("메이커1 완료 리워드2 소개")
            .requiredAmount(new BigDecimal("2000"))
            .maxQuantity(10L)
            .remainQuantity(9L)
            .provideDate(now.plusDays(60))
            .build();

    public Donate donateUser1CompleteDonate = Donate.builder()
            .pk(1L)
            .user(donateUser1)
            .reward(maker1CompleteReward1)
            .amount(new BigDecimal("1000"))
            .quantity(1L)
            .state(true)
            .build();
    public Donate donateUser2CompleteDonate = Donate.builder()
            .pk(2L)
            .user(donateUser2)
            .reward(maker1CompleteReward2)
            .amount(new BigDecimal("2000"))
            .quantity(1L)
            .state(false)
            .build();
    public Donate donateUser1ApproveDonate = Donate.builder()
            .pk(3L)
            .user(donateUser1)
            .reward(maker1ApproveReward1)
            .amount(new BigDecimal("1000"))
            .quantity(1L)
            .state(false)
            .build();
    public Donate donateUser2ApproveDonate = Donate.builder()
            .pk(4L)
            .user(donateUser2)
            .reward(maker1ApproveReward2)
            .amount(new BigDecimal("2000"))
            .quantity(1L)
            .state(false)
            .build();

    private InitDB() {
    }

    //DB 초기화. 처음 한 번, 또는 쌓인 테스트 데이터를 비우고 싶을 때 직접 실행한다
    public static void main(String[] args) {
        drop();
        create();
        init();
    }

    //기본 데이터의 pk와 값이 담긴 객체를 돌려준다. DB를 조회하거나 변경하지 않는다
    public static InitDB load() {
        return new InitDB();
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

    //필드에 정해둔 기본 데이터를 리포지토리로 삽입. 필드의 pk와 맞도록 순서를 유지한다
    private void insert() {
        List.of(maker1, maker2, donateUser1, donateUser2, newUser)
                .forEach(config.getUserRepository()::create);

        List.of(food, tech, furniture, travel)
                .forEach(config.getCategoryRepository()::create);

        List.of(maker1PendingFunding, maker2PendingFunding, maker1ApproveFunding, maker2ApproveFunding, maker1CompleteFunding)
                .forEach(config.getFundingRepository()::create);

        bindCategory(maker1PendingFunding, food);
        bindCategory(maker2PendingFunding, tech);
        bindCategory(maker1ApproveFunding, furniture);
        bindCategory(maker1ApproveFunding, tech);
        bindCategory(maker2ApproveFunding, tech);
        bindCategory(maker1CompleteFunding, travel);
        bindCategory(maker1CompleteFunding, food);

        List.of(maker1PendingReward1, maker1PendingReward2,
                        maker2PendingReward1, maker2PendingReward2,
                        maker1ApproveReward1, maker1ApproveReward2,
                        maker2ApproveReward1, maker2ApproveReward2,
                        maker1CompleteReward1, maker1CompleteReward2)
                .forEach(config.getRewardRepository()::create);

        List.of(donateUser1CompleteDonate, donateUser2CompleteDonate, donateUser1ApproveDonate, donateUser2ApproveDonate)
                .forEach(config.getDonateRepository()::create);
    }

    private void bindCategory(Funding funding, Category category) {
        config.getFundingCategoryRepository().create(FundingCategory.builder()
                .funding(funding)
                .category(category)
                .build());
    }
}
