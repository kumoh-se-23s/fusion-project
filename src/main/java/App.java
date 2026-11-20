import com.minihttp.MiniHttp;
import com.minihttp.config.ServerConfig;
import config.AppConfig;
import controller.external.*;
import controller.internal.*;
import repository.*;
import service.*;
import service.external.*;
import service.internal.*;
import util.JwtAuthenticationMiddleware;
import util.JwtTokenProvider;

public class App {
    public static void main(String[] args) throws Exception {
        AppConfig app = new AppConfig();

        MiniHttp.create()
                // Repositories
                .bind(CategoryRepository.class, CategoryRepository.class)
                .bind(DenyProjectRepository.class, DenyProjectRepository.class)
                .bind(DonateRepository.class, DonateRepository.class)
                .bind(ProjectLikeRepository.class, ProjectLikeRepository.class)
                .bind(ProjectRepository.class, ProjectRepository.class)
                .bind(ReviewLikeRepository.class, ReviewLikeRepository.class)
                .bind(ReviewRepository.class, ReviewRepository.class)
                .bind(RewardRepository.class, RewardRepository.class)
                .bind(SocialAccountRepository.class, SocialAccountRepository.class)
                .bind(UserRepository.class, UserRepository.class)
                .bind(UserRoleRelationRepository.class, UserRoleRelationRepository.class)
                .bind(UserRoleRepository.class, UserRoleRepository.class)
                // External Services
                .bind(AuthService.class, AuthService.class)
                .bind(CategoryService.class, CategoryService.class)
                .bind(DonateService.class, DonateService.class)
                .bind(ProfileService.class, ProfileService.class)
                .bind(ProjectLikeService.class, ProjectLikeService.class)
                .bind(ProjectMakerService.class, ProjectMakerService.class)
                .bind(ProjectQueryService.class, ProjectQueryService.class)
                .bind(ReviewCommandService.class, ReviewCommandService.class)
                .bind(ReviewLikeService.class, ReviewLikeService.class)
                .bind(ReviewQueryService.class, ReviewQueryService.class)
                .bind(RewardService.class, RewardService.class)
                .bind(SocialAuthService.class, SocialAuthService.class)
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
                .bind(BaseService.class, AdminUserRoleService.class)
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
                .middleware(new JwtAuthenticationMiddleware(new JwtTokenProvider()))
                .run(ServerConfig.builder().listen(8080).build());
    }
}
