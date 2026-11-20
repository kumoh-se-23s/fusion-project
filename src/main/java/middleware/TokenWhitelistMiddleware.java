package middleware;

import com.minihttp.core.handler.Handler;
import com.minihttp.core.middleware.Middleware;
import com.minihttp.packet.HttpRequest;
import com.minihttp.packet.HttpResponse;
import exception.InvalidException;
import exception.UnauthorizedException;
import util.JwtTokenProvider;
import util.RedisUtil;

import java.util.Objects;

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
            if (Objects.equals(tokenType, "access")) {
                return next.handle(request);
            } else if (Objects.equals(tokenType, "refresh")) {
                String newRefreshToken = jwtTokenProvider.createRefreshToken(userId);
                String newJti = jwtTokenProvider.getJti(newRefreshToken);
                redisUtil.save("refresh" + userId + " : ", newJti, 1209600);

                request.header().set("Authorization", BEARER_PREFIX + newRefreshToken);
                return next.handle(request);
            } else {
                throw new InvalidException();
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
