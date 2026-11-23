import com.minihttp.MiniHttp;
import com.minihttp.config.ServerConfig;
import com.minihttp.core.middleware.CorsMiddleware;
import config.AppConfig;
import controller.external.*;
import controller.internal.*;


public class App {
    public static void main(String[] args) throws Exception {
        AppConfig app = new AppConfig();
        MiniHttp.create()
                // External Routers
                .includeRouter(CategoryRouter.class)
                .includeRouter(DonateRouter.class)
                .includeRouter(ProjectLikeRouter.class)
                .includeRouter(ProjectRouter.class)
                .includeRouter(ReviewLikeRouter.class)
                .includeRouter(ReviewRouter.class)
                .includeRouter(RewardRouter.class)
                .includeRouter(AccountRouter.class)
                .includeRouter(AuthRouter.class)
                .includeRouter(UserRouter.class)
                // Internal Routers
                .includeRouter(AdminCategoryRouter.class)
                .includeRouter(AdminDenyProjectRouter.class)
                .includeRouter(AdminDonateRouter.class)
                .includeRouter(AdminProjectRouter.class)
                .includeRouter(AdminReviewRouter.class)
                .includeRouter(AdminRewardRouter.class)
                .includeRouter(AdminAccountRouter.class)
                .includeRouter(AdminUserRoleRouter.class)
                .includeRouter(AdminUserRouter.class)
                .middleware(CorsMiddleware.builder().build())
                .run(ServerConfig.builder().listen(8080).build());
    }
}
