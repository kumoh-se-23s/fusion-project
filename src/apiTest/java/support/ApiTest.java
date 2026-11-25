package support;

import model.domain.AppUser;
import repository.UserRepository;
import util.JwtTokenProvider;
import constant.RedisKey;
import util.RedisUtil;

import java.util.UUID;

/**
 * 엔드포인트 테스트의 부모 클래스.
 *
 * <p>구조: 라우터 하나당 테스트 클래스 하나, 그 안에 엔드포인트마다 {@code @Nested @ApiEndpoint} 클래스,
 * 그 안에 다시 두 갈래를 둔다.
 * <ul>
 *   <li>{@code Http}: 클라이언트 -> 엔드포인트. {@link #client}로 실제 요청을 보내고 상태 코드, 응답 JSON,
 *       인증, 파라미터 바인딩, 에러 응답을 검증한다.</li>
 *   <li>{@code Logic}: 엔드포인트가 부르는 서비스 메서드를 {@link #bean}으로 꺼내 직접 호출해서
 *       분기와 예외, DB 반영 결과를 검증한다.</li>
 * </ul>
 *
 * <p>DB와 Redis는 개발 서버의 실제 인스턴스를 쓴다. 테스트끼리 데이터가 겹치지 않도록
 * {@link #unique(String)}로 값을 만든다.
 */
public abstract class ApiTest {
    protected final TestServer server = TestServer.get();
    protected final ApiClient client = server.client();

    protected <T> T bean(Class<T> type) {
        return server.bean(type);
    }

    /** 테스트마다 겹치지 않는 문자열. 예: unique("email") -> "email-1a2b3c4d5e6f" */
    protected static String unique(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    /** 필수 값만 채운 유저를 DB에 만든다. */
    protected AppUser createUser() {
        String key = unique("u");
        return bean(UserRepository.class).create(AppUser.builder()
                .name("테스트 " + key)
                .nickname(key)
                .email(key + "@test.com")
                .build());
    }

    /**
     * 로그인한 것과 같은 상태를 만든다. 로그인 API가 하는 것처럼 access/refresh 토큰의 jti를 Redis에 등록하고
     * access 토큰을 돌려준다.
     */
    protected String login(long userId) {
        JwtTokenProvider jwt = bean(JwtTokenProvider.class);
        RedisUtil redis = bean(RedisUtil.class);
        String accessToken = jwt.createAccessToken(userId);
        String refreshToken = jwt.createRefreshToken(userId);
        redis.save(RedisKey.ACCESS, userId, jwt.getJti(accessToken), JwtTokenProvider.ACCESS_TOKEN_EXPIRE_SECONDS);
        redis.save(RedisKey.REFRESH, userId, jwt.getJti(refreshToken), JwtTokenProvider.REFRESH_TOKEN_EXPIRE_SECONDS);
        return accessToken;
    }

    protected String login(AppUser user) {
        return login(user.getId());
    }
}
