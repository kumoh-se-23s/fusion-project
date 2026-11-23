import com.minihttp.MiniHttp;
import com.minihttp.config.ServerConfig;
import com.minihttp.core.middleware.CorsMiddleware;
import config.AppConfig;
import config.AuthProperties;
import controller.external.*;
import controller.internal.*;
import service.OAuthClient;
import util.RedisUtil;

import java.net.http.HttpClient;


public class App {
    public static void main(String[] args) throws Exception {
        AppConfig app = new AppConfig();

        // 자동 주입이 안 되는 것만 직접 등록 (OAuthClient: HttpClient·String 생성자 인자)
        RedisUtil redisUtil = new RedisUtil();
        OAuthClient oAuthClient = new OAuthClient(
                redisUtil,
                HttpClient.newHttpClient(),
                AuthProperties.get("oauth.client-id", ""),
                AuthProperties.get("oauth.client-secret", ""),
                AuthProperties.get("oauth.redirect-uri", ""));

        MiniHttp.create()
                .provide(RedisUtil.class, redisUtil)
                .provide(OAuthClient.class, oAuthClient)
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
