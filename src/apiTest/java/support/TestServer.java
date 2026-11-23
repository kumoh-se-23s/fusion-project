package support;

import com.minihttp.MiniHttp;
import com.minihttp.core.handler.Handler;
import com.minihttp.core.inject.Container;
import com.minihttp.core.middleware.CorsMiddleware;
import com.minihttp.core.route.BaseRouter;
import config.AppConfig;
import config.AuthProperties;
import controller.external.*;
import controller.internal.*;
import exception.BusinessException;
import handler.BusinessExceptionHandler;
import service.OAuthClient;
import util.RedisUtil;

import java.net.http.HttpClient;
import java.util.List;

/**
 * App.main과 같은 구성으로 서버 핸들러를 만든다. 소켓은 열지 않고 요청 -> 응답을 메모리 안에서 처리한다.
 * DB와 Redis는 실제 개발 서버(apsode-linux-pc)를 그대로 쓴다.
 *
 * <p>JVM 안에서 한 번만 만든다. AppConfig 생성 시 db.ddl-auto 설정에 따라 테이블이 다시 만들어진다.
 *
 * <p>라우터 목록은 App.main과 똑같이 유지해야 한다. App에 라우터를 추가하면 여기에도 추가한다.
 */
public final class TestServer {
    public static final List<Class<? extends BaseRouter>> ROUTERS = List.of(
            // External Routers
            CategoryRouter.class,
            DonateRouter.class,
            ProjectLikeRouter.class,
            ProjectRouter.class,
            ReviewLikeRouter.class,
            ReviewRouter.class,
            RewardRouter.class,
            AccountRouter.class,
            AuthRouter.class,
            UserRouter.class,
            // Internal Routers
            AdminCategoryRouter.class,
            AdminDenyProjectRouter.class,
            AdminDonateRouter.class,
            AdminProjectRouter.class,
            AdminReviewRouter.class,
            AdminRewardRouter.class,
            AdminAccountRouter.class,
            AdminUserRoleRouter.class,
            AdminUserRouter.class
    );

    private static TestServer instance;

    private final Container container;
    private final Handler handler;

    private TestServer() {
        new AppConfig();

        this.container = Container.create();
        RedisUtil redisUtil = new RedisUtil();
        OAuthClient oAuthClient = new OAuthClient(
                redisUtil,
                HttpClient.newHttpClient(),
                AuthProperties.get("oauth.client-id", ""),
                AuthProperties.get("oauth.client-secret", ""),
                AuthProperties.get("oauth.redirect-uri", ""));
        this.container.provide(RedisUtil.class, redisUtil);
        this.container.provide(OAuthClient.class, oAuthClient);

        // 라우터와 서비스, 리포지토리를 테스트 쪽 컨테이너에서 만든다.
        // 그래야 로직 테스트가 HTTP 요청이 쓰는 것과 같은 서비스 인스턴스를 꺼내 쓸 수 있다.
        MiniHttp app = MiniHttp.create();
        for (Class<? extends BaseRouter> routerType : ROUTERS) {
            app.includeRouter(this.container.get(routerType));
        }
        this.handler = app
                .middleware(CorsMiddleware.builder().build())
                .exceptionHandler(BusinessException.class, new BusinessExceptionHandler())
                .buildHandler();
    }

    public static synchronized TestServer get() {
        if (instance == null) {
            instance = new TestServer();
        }
        return instance;
    }

    public Handler handler() {
        return this.handler;
    }

    /** 서비스, 리포지토리, 유틸 등 앱이 쓰는 것과 같은 인스턴스를 꺼낸다. */
    public <T> T bean(Class<T> type) {
        return this.container.get(type);
    }

    public ApiClient client() {
        return new ApiClient(this.handler);
    }
}
