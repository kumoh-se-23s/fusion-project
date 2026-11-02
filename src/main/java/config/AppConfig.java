package config;

import lombok.Getter;
import repository.*;
import repository.manager.schema.SchemaGenerator;
import service.external.DonateService;
import service.external.ProjectService;
import service.external.ReviewService;
import service.external.RewardService;
import service.internal.CategoryService;

/**
 * 앱 시작 시 리포지토리, 서비스를 한 번 생성하고 주입하는 조립 클래스
 * 새 서비스를 만들면 여기에 등록한다.
 */
@Getter
public class AppConfig {
    private final CategoryRepository categoryRepository = new CategoryRepository();
    private final DenyProjectRepository denyProjectRepository = new DenyProjectRepository();
    private final DonateRepository donateRepository = new DonateRepository();
    private final ProjectCategoryRepository projectCategoryRepository = new ProjectCategoryRepository();
    private final ProjectLikeRepository projectLikeRepository = new ProjectLikeRepository();
    private final ProjectRepository projectRepository = new ProjectRepository();
    private final ReviewLikeRepository reviewLikeRepository = new ReviewLikeRepository();
    private final ReviewRepository reviewRepository = new ReviewRepository();
    private final RewardRepository rewardRepository = new RewardRepository();
    private final UserRepository userRepository = new UserRepository();
    private final UserRoleRepository userRoleRepository = new UserRoleRepository();
    private final UserRoleRelationRepository userRoleRelationRepository = new UserRoleRelationRepository();

    private final CategoryService categoryService = new CategoryService(categoryRepository);
    private final ProjectService projectService = new ProjectService(
            projectRepository, donateRepository, rewardRepository,
            userRepository, categoryRepository, projectCategoryRepository);
    private final service.external.RewardService rewardService = new RewardService(
            projectRepository, donateRepository, rewardRepository, categoryRepository);
    private final service.external.DonateService donateService = new DonateService(
            donateRepository, rewardRepository, projectRepository, userRepository
    );
    //이름이 같은 external.ProjectService와 구분하기 위해 전체 경로 사용
    private final service.internal.ProjectService adminProjectService =
            new service.internal.ProjectService(projectRepository);
    private final ReviewService reviewService = new ReviewService(
            reviewRepository, userRepository, donateRepository, rewardRepository);

    public AppConfig() {
        //db.ddl-auto 설정에 따라 도메인 기준으로 테이블 생성
        SchemaGenerator.Mode ddlMode = SchemaGenerator.Mode.from(PropertieData.get("db.ddl-auto", "none"));
        new SchemaGenerator().execute(ddlMode, "model.domain");
    }
}
