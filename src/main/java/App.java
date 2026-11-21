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
//        AppConfig app = new AppConfig();
        MiniHttp.create()
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
//                .middleware(new JwtAuthenticationMiddleware(new JwtTokenProvider()))
                .run(ServerConfig.builder().listen(8080).build());
    }
}
