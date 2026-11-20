import com.minihttp.MiniHttp;
import com.minihttp.config.ServerConfig;
import config.AppConfig;
import controller.external.*;
import controller.internal.*;
import repository.*;
import service.*;
import service.external.*;
import service.internal.*;

public class App {
    public static void main(String[] args) throws Exception {
        AppConfig app = new AppConfig();

        MiniHttp.create()
                // Repositories
                .bind(AccountRepository.class, AccountRepository.class)
                .bind(CategoryRepository.class, CategoryRepository.class)
                .bind(DenyProjectRepository.class, DenyProjectRepository.class)
                .bind(DonateRepository.class, DonateRepository.class)
                .bind(ProjectLikeRepository.class, ProjectLikeRepository.class)
                .bind(ProjectRepository.class, ProjectRepository.class)
                .bind(ReviewLikeRepository.class, ReviewLikeRepository.class)
                .bind(ReviewRepository.class, ReviewRepository.class)
                .bind(RewardRepository.class, RewardRepository.class)
                .bind(UserRepository.class, UserRepository.class)
                .bind(UserRoleRelationRepository.class, UserRoleRelationRepository.class)
                .bind(UserRoleRepository.class, UserRoleRepository.class)
                // Services
                .bind(AccountService.class, AccountService.class)
                .bind(TokenService.class, TokenService.class)
                .bind(UserService.class, UserService.class)
                // External Services
                .bind(CategoryService.class, CategoryService.class)
                .bind(DonateService.class, DonateService.class)
                .bind(ProjectLikeService.class, ProjectLikeService.class)
                .bind(ProjectMakerService.class, ProjectMakerService.class)
                .bind(ProjectQueryService.class, ProjectQueryService.class)
                .bind(ReviewCommandService.class, ReviewCommandService.class)
                .bind(ReviewLikeService.class, ReviewLikeService.class)
                .bind(ReviewQueryService.class, ReviewQueryService.class)
                .bind(RewardService.class, RewardService.class)
                // Internal Services
                .bind(AdminCategoryService.class, AdminCategoryService.class)
                .bind(AdminDenyProjectControlService.class, AdminDenyProjectControlService.class)
                .bind(AdminDenyProjectQueryService.class, AdminDenyProjectQueryService.class)
                .bind(AdminDonateService.class, AdminDonateService.class)
                .bind(AdminProjectControlService.class, AdminProjectControlService.class)
                .bind(AdminProjectQueryService.class, AdminProjectQueryService.class)
                .bind(AdminReviewControlService.class, AdminReviewControlService.class)
                .bind(AdminReviewQueryService.class, AdminReviewQueryService.class)
                .bind(AdminRewardService.class, AdminRewardService.class)
                // External Routers
                .includeRouter(CategoryRouter.class)
                .includeRouter(DonateRouter.class)
                .includeRouter(ProjectLikeRouter.class)
                .includeRouter(ProjectRouter.class)
                .includeRouter(ReviewLikeRouter.class)
                .includeRouter(ReviewRouter.class)
                .includeRouter(RewardRouter.class)
                // Internal Routers
                .includeRouter(AdminCategoryRouter.class)
                .includeRouter(AdminDenyProjectRouter.class)
                .includeRouter(AdminDonateRouter.class)
                .includeRouter(AdminProjectRouter.class)
                .includeRouter(AdminReviewRouter.class)
                .includeRouter(AdminRewardRouter.class)
                .run(ServerConfig.builder().listen(8080).build());
    }
}
