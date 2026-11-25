package middleware;

import com.minihttp.core.handler.Handler;
import com.minihttp.core.middleware.Middleware;
import com.minihttp.core.response.JsonResponse;
import com.minihttp.packet.HttpRequest;
import com.minihttp.packet.HttpResponse;
import constant.RedisKey;
import exception.InvalidTokenException;
import exception.UnauthorizedException;
import model.dto.response.TokenResponse;
import util.JwtTokenProvider;
import util.RedisUtil;

import java.util.Objects;

public class TokenWhitelistMiddleware implements Middleware {
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;
    private final RedisUtil redisUtil;

    public TokenWhitelistMiddleware(JwtTokenProvider jwtTokenProvider, RedisUtil redisUtil) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.redisUtil = redisUtil;
    }

    @Override
    public HttpResponse handle(HttpRequest request, Handler next) throws Exception {
        String jwt = extractToken(request);

        String jti = jwtTokenProvider.getJti(jwt);
        long userId = jwtTokenProvider.getUserId(jwt);
        String tokenType = jwtTokenProvider.getTokenType(jwt);

        if (redisUtil.isRegisteredInRedis(tokenType, jti, userId)) {
            //AccessToken이 들어온 경우
            if (Objects.equals(tokenType, "access")) {
                return next.handle(request);
            }
            //RefreshToken이 들어온 경우
            else if (Objects.equals(tokenType, "refresh")) {
                String newAccessToken = jwtTokenProvider.createAccessToken(userId);
                String newRefreshToken = jwtTokenProvider.createRefreshToken(userId);

                String newAccessJti = jwtTokenProvider.getJti(newAccessToken);
                String newRefreshJti = jwtTokenProvider.getJti(newRefreshToken);

                redisUtil.save(RedisKey.ACCESS, userId, newAccessJti, JwtTokenProvider.ACCESS_TOKEN_EXPIRE_SECONDS);
                redisUtil.save(RedisKey.REFRESH, userId, newRefreshJti, JwtTokenProvider.REFRESH_TOKEN_EXPIRE_SECONDS);

                return new JsonResponse(new TokenResponse(newAccessToken, newRefreshToken));
            } else {
                throw new InvalidTokenException();
            }
        }

        throw new UnauthorizedException();
    }

    private String extractToken(HttpRequest request) {
        String value = request.header().get("Authorization")
                .orElseThrow(UnauthorizedException::new);

        if (!value.startsWith(BEARER_PREFIX)) {
            throw new UnauthorizedException();
        }

        String token = value.substring(BEARER_PREFIX.length()).trim();

        if (token.isEmpty()) {
            throw new UnauthorizedException();
        }

        return token;
    }
}
