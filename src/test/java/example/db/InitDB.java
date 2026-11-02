package example.db;

import config.AppConfig;
import config.PooledDataSource;
import constant.ProjectState;
import model.domain.*;
import repository.manager.query_dsl.EntityMetadata;
import repository.manager.schema.SchemaGenerator;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;

public class InitDB {
    private static final List<Class<?>> ENTITIES = List.of(
            AppUser.class, UserRole.class, UserRoleRelation.class, LocalAccount.class, Category.class,
            Project.class, DenyProject.class, ProjectCategory.class, ProjectLike.class,
            Reward.class, Donate.class, DonateCancel.class, Review.class, ReviewLike.class);

    public final AppConfig config = new AppConfig();

    private final LocalDateTime now = LocalDateTime.now();

    public AppUser maker1 = AppUser.builder()
            .id(1L)
            .name("[시연 0] 메이커1")
            .address("[시연 0] 메이커1 주소")
            .email("[시연 0] 메이커1 이메일")
            .phone("[시연 0] 메이커1 전화번호")
            .postal("[시연 0] 메이커1 우편번호")
            .build();
    public AppUser maker2 = AppUser.builder()
            .id(2L)
            .name("[시연 0] 메이커2")
            .address("[시연 0] 메이커2 주소")
            .email("[시연 0] 메이커2 이메일")
            .phone("[시연 0] 메이커2 전화번호")
            .postal("[시연 0] 메이커2 우편번호")
            .build();

    public AppUser donateUser1 = AppUser.builder()
            .id(3L)
            .name("[시연 0] 후원자1")
            .address("[시연 0] 후원자1 주소")
            .email("[시연 0] 후원자1 이메일")
            .phone("[시연 0] 후원자1 전화번호")
            .postal("[시연 0] 후원자1 우편번호")
            .build();
    public AppUser donateUser2 = AppUser.builder()
            .id(4L)
            .name("[시연 0] 후원자2")
            .address("[시연 0] 후원자2 주소")
            .email("[시연 0] 후원자2 이메일")
            .phone("[시연 0] 후원자2 전화번호")
            .postal("[시연 0] 후원자2 우편번호")
            .build();

    public AppUser newUser = AppUser.builder()
            .id(5L)
            .name("[시연 0] 신규 사용자")
            .address("[시연 0] 신규 사용자 주소")
            .email("[시연 0] 신규 사용자 이메일")
            .phone("[시연 0] 신규 사용자 전화번호")
            .postal("[시연 0] 신규 사용자 우편번호")
            .build();

    public AppUser notDonateUser1 = AppUser.builder()
            .id(6L)
            .name("[시연 08] 후원하지 않은 사용자1")
            .address("[시연 08] 후원하지 않은 사용자1 주소")
            .email("[시연 08] 후원하지 않은 사용자1 이메일")
            .phone("[시연 08] 후원하지 않은 사용자1 전화번호")
            .postal("[시연 08] 미후원자1 우편번호")
            .build();

    public AppUser notDonateUser2 = AppUser.builder()
            .id(7L)
            .name("[시연 10] 후원하지 않은 사용자2")
            .address("[시연 10] 후원하지 않은 사용자2 주소")
            .email("[시연 10] 후원하지 않은 사용자2 이메일")
            .phone("[시연 10] 후원하지 않은 사용자2 전화번호")
            .postal("[시연 10] 미후원자2 우편번호")
            .build();

    public AppUser notDonateUser3 = AppUser.builder()
            .id(8L)
            .name("[시연 10] 후원하지 않은 사용자3")
            .address("[시연 10] 후원하지 않은 사용자3 주소")
            .email("[시연 10] 후원하지 않은 사용자3 이메일")
            .phone("[시연 10] 후원하지 않은 사용자3 전화번호")
            .postal("[시연 10] 미후원자3 우편번호")
            .build();

    public Category food = Category.builder()
            .id(1L)
            .name("[시연 0] food")
            .description("[시연 0] 음식")
            .build();

    public Category tech = Category.builder()
            .id(2L)
            .name("[시연 0] tech")
            .description("[시연 0] 기술")
            .build();
    public Category furniture = Category.builder()
            .id(3L)
            .name("[시연 0] furniture")
            .description("[시연 0] 가구").build();
    public Category travel = Category.builder()
            .id(4L)
            .name("[시연 0] travel")
            .description("[시연 0] 여행")
            .build();

    public Project maker1NoRewardProject = Project.builder()
            .id(7L)
            .maker(maker1)
            .title("[시연 0] 메이커1 리워드 없는 프로젝트")
            .text("[시연 0] 메이커1 리워드 없는 소개")
            .state(ProjectState.JUST_USER_INPUT)
            .purposeAmount(new BigDecimal("100000"))
            .endDate(now.plusDays(30))
            .createdAt(LocalDateTime.now().minusDays(10))
            .build();

    public Project maker1PendingProject = Project.builder()
            .id(1L)
            .maker(maker1)
            .title("[시연 0] 메이커1 승인 대기 프로젝트")
            .text("[시연 0] 메이커1 승인 대기 프로젝트 소개")
            .state(ProjectState.PENDING_APPROVAL)
            .purposeAmount(new BigDecimal("100000"))
            .endDate(now.plusDays(30))
            .createdAt(now.minusDays(9))
            .build();

    public Project maker2PendingProject = Project.builder()
            .id(2L)
            .maker(maker2)
            .title("[시연 0] 메이커2 승인 대기 프로젝트")
            .text("[시연 0] 메이커2 승인 대기 프로젝트 소개")
            .state(ProjectState.PENDING_APPROVAL)
            .purposeAmount(new BigDecimal("100000"))
            .endDate(now.plusDays(30))
            .createdAt(now.minusDays(8))
            .build();

    public Project maker1ApproveProject = Project.builder()
            .id(3L)
            .maker(maker1)
            .title("[시연 0] 메이커1 승인 프로젝트")
            .text("[시연 0] 메이커1 승인 프로젝트 소개")
            .state(ProjectState.APPROVED)
            .purposeAmount(new BigDecimal("100000"))
            .donateAmount(new BigDecimal("3000"))
            .endDate(now.plusDays(30))
            .createdAt(now.minusDays(7))
            .build();
    public Project maker2ApproveProject = Project.builder()
            .id(4L)
            .maker(maker2)
            .title("[시연 0] 메이커2 승인 프로젝트")
            .text("[시연 0] 메이커2 승인 프로젝트 소개")
            .state(ProjectState.APPROVED)
            .purposeAmount(new BigDecimal("100000"))
            .endDate(now.plusDays(30))
            .createdAt(now.minusDays(6))
            .build();

    public Project maker1CompleteProject = Project.builder()
            .id(5L)
            .maker(maker1)
            .title("[시연 0] 메이커1 완료 프로젝트")
            .text("[시연 0] 메이커1 완료 프로젝트 소개")
            .state(ProjectState.COMPLETED)
            .purposeAmount(new BigDecimal("3000"))
            .donateAmount(new BigDecimal("3000"))
            .endDate(now.plusDays(30))
            .createdAt(now.minusDays(5))
            .build();

    public Project changeStateProject = Project.builder()
            .id(6L)
            .maker(maker1)
            .title("[시연 04] 승인용 프로젝트")
            .text("[시연 04] 승인용 프로젝트 소개")
            .state(ProjectState.PENDING_APPROVAL)
            .purposeAmount(new BigDecimal("1"))
            .donateAmount(new BigDecimal("1"))
            .endDate(now.plusDays(30))
            .createdAt(now.minusDays(4))
            .build();

    public Reward maker1PendingReward1 = Reward.builder()
            .id(1L)
            .project(maker1PendingProject)
            .category(food)
            .name("[시연 0] 메이커1 승인 대기 리워드1")
            .text("[시연 0] 메이커1 승인 대기 리워드1 소개")
            .requiredAmount(new BigDecimal("1000"))
            .maxQuantity(10L)
            .remainQuantity(10L)
            .provideDate(now.plusDays(60))
            .build();
    public Reward maker1PendingReward2 = Reward.builder()
            .id(2L)
            .project(maker1PendingProject)
            .category(food)
            .name("[시연 0] 메이커1 승인 대기 리워드2")
            .text("[시연 0] 메이커1 승인 대기 리워드2 소개")
            .requiredAmount(new BigDecimal("2000"))
            .maxQuantity(10L)
            .remainQuantity(10L)
            .provideDate(now.plusDays(60))
            .build();

    public Reward maker2PendingReward1 = Reward.builder()
            .id(3L)
            .project(maker2PendingProject)
            .category(tech)
            .name("[시연 0] 메이커2 승인 대기 리워드1")
            .text("[시연 0] 메이커2 승인 대기 리워드1 소개")
            .requiredAmount(new BigDecimal("1000"))
            .maxQuantity(10L)
            .remainQuantity(10L)
            .provideDate(now.plusDays(60))
            .build();
    public Reward maker2PendingReward2 = Reward.builder()
            .id(4L)
            .project(maker2PendingProject)
            .category(tech)
            .name("[시연 0] 메이커2 승인 대기 리워드2")
            .text("[시연 0] 메이커2 승인 대기 리워드2 소개")
            .requiredAmount(new BigDecimal("2000"))
            .maxQuantity(10L)
            .remainQuantity(10L)
            .provideDate(now.plusDays(60))
            .build();

    public Reward maker1ApproveReward1 = Reward.builder()
            .id(5L)
            .project(maker1ApproveProject)
            .category(furniture)
            .name("[시연 0] 메이커1 승인 리워드1")
            .text("[시연 0] 메이커1 승인 리워드1 소개")
            .requiredAmount(new BigDecimal("1000"))
            .maxQuantity(10L)
            .remainQuantity(9L)
            .provideDate(now.plusDays(60))
            .build();

    public Reward maker1ApproveReward2 = Reward.builder()
            .id(6L)
            .project(maker1ApproveProject)
            .category(furniture)
            .name("[시연 0] 메이커1 승인 리워드2")
            .text("[시연 0] 메이커1 승인 리워드2 소개")
            .requiredAmount(new BigDecimal("2000"))
            .maxQuantity(10L)
            .remainQuantity(9L)
            .provideDate(now.plusDays(60))
            .build();

    public Reward maker2ApproveReward1 = Reward.builder()
            .id(7L)
            .project(maker2ApproveProject)
            .category(tech)
            .name("[시연 0] 메이커2 승인 리워드1")
            .text("[시연 0] 메이커2 승인 리워드1 소개")
            .requiredAmount(new BigDecimal("1000"))
            .maxQuantity(10L)
            .remainQuantity(10L)
            .provideDate(now.plusDays(60))
            .build();
    public Reward maker2ApproveReward2 = Reward.builder()
            .id(8L)
            .project(maker2ApproveProject)
            .category(tech)
            .name("[시연 0] 메이커2 승인 리워드2")
            .text("[시연 0] 메이커2 승인 리워드2 소개")
            .requiredAmount(new BigDecimal("2000"))
            .maxQuantity(10L)
            .remainQuantity(10L)
            .provideDate(now.plusDays(60))
            .build();

    public Reward maker1CompleteReward1 = Reward.builder()
            .id(9L)
            .project(maker1CompleteProject)
            .category(travel)
            .name("[시연 0] 메이커1 완료 리워드1")
            .text("[시연 0] 메이커1 완료 리워드1 소개")
            .requiredAmount(new BigDecimal("1000"))
            .maxQuantity(10L)
            .remainQuantity(9L)
            .provideDate(now.plusDays(60))
            .build();

    public Reward maker1CompleteReward2 = Reward.builder()
            .id(10L)
            .project(maker1CompleteProject)
            .category(travel)
            .name("[시연 0] 메이커1 완료 리워드2")
            .text("[시연 0] 메이커1 완료 리워드2 소개")
            .requiredAmount(new BigDecimal("2000"))
            .maxQuantity(10L)
            .remainQuantity(9L)
            .provideDate(now.plusDays(60))
            .build();
    public Reward maker1ConcurrencyReward1 = Reward.builder()
            .id(11L)
            .project(maker1ApproveProject)
            .category(travel)
            .name("[시연 10] 동시성 테스트용 리워드 1")
            .text("[시연 10] 동시성 테스트용 리워드 1 소개")
            .requiredAmount(new BigDecimal("2000"))
            .maxQuantity(12L)
            .remainQuantity(2L)
            .provideDate(now.plusDays(60))
            .build();
    public Donate donateUser1CompleteDonate = Donate.builder()
            .id(1L)
            .user(donateUser1)
            .reward(maker1CompleteReward1)
            .amount(new BigDecimal("1000"))
            .quantity(1L)
            .state(true)
            .build();
    public Donate donateUser2CompleteDonate = Donate.builder()
            .id(2L)
            .user(donateUser2)
            .reward(maker1CompleteReward2)
            .amount(new BigDecimal("2000"))
            .quantity(1L)
            .state(false)
            .build();
    public Donate donateUser1ApproveDonate = Donate.builder()
            .id(3L)
            .user(donateUser1)
            .reward(maker1ApproveReward1)
            .amount(new BigDecimal("1000"))
            .quantity(1L)
            .state(false)
            .build();
    public Donate donateUser2ApproveDonate = Donate.builder()
            .id(4L)
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

    //기본 데이터의 id와 값이 담긴 객체를 돌려준다. DB를 조회하거나 변경하지 않는다
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

    //필드에 정해둔 기본 데이터를 리포지토리로 삽입. 필드의 id와 맞도록 순서를 유지한다
    private void insert() {
        List.of(maker1, maker2, donateUser1, donateUser2, newUser, notDonateUser1, notDonateUser2, notDonateUser3)
                .forEach(config.getUserRepository()::create);

        List.of(food, tech, furniture, travel)
                .forEach(config.getCategoryRepository()::create);

        List.of(maker1PendingProject, maker2PendingProject, maker1ApproveProject,
                maker2ApproveProject, maker1CompleteProject, changeStateProject,
                        maker1NoRewardProject)
                .forEach(config.getProjectRepository()::create);

        bindCategory(maker1PendingProject, food);
        bindCategory(maker2PendingProject, tech);
        bindCategory(maker1ApproveProject, furniture);
        bindCategory(maker1ApproveProject, tech);
        bindCategory(maker2ApproveProject, tech);
        bindCategory(maker1CompleteProject, travel);
        bindCategory(maker1CompleteProject, food);
        bindCategory(maker1NoRewardProject, food);

        List.of(maker1PendingReward1, maker1PendingReward2,
                        maker2PendingReward1, maker2PendingReward2,
                        maker1ApproveReward1, maker1ApproveReward2,
                        maker2ApproveReward1, maker2ApproveReward2,
                        maker1CompleteReward1, maker1CompleteReward2,
                        maker1ConcurrencyReward1)
                .forEach(config.getRewardRepository()::create);

        List.of(donateUser1CompleteDonate, donateUser2CompleteDonate, donateUser1ApproveDonate, donateUser2ApproveDonate)
                .forEach(config.getDonateRepository()::create);
    }

    private void bindCategory(Project project, Category category) {
        config.getProjectCategoryRepository().create(ProjectCategory.builder()
                .project(project)
                .category(category)
                .build());
    }
}
