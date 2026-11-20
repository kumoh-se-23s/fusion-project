package util;

import com.minihttp.core.handler.Handler;
import com.minihttp.core.middleware.Middleware;
import com.minihttp.packet.HttpRequest;
import com.minihttp.packet.HttpResponse;
import exception.NotFoundException;
import exception.UnauthorizedException;

import java.util.Optional;

public class TokenWhitelistMiddleware implements Middleware {
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisUtil redisUtil;

    public TokenWhitelistMiddleware(JwtTokenProvider jwtTokenProvider, RedisUtil redisUtil) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.redisUtil = redisUtil;
    }

    @Override
    public HttpResponse handle(HttpRequest request, Handler next) throws Exception {
        Optional<String> authHeader = request.header().get("Authorization");

        String[] parts = authHeader.orElseThrow(NotFoundException::new).split(" ");

        String jwt = parts[1];

        String jti = jwtTokenProvider.getJti(jwt);
        long userId = jwtTokenProvider.getUserId(jwt);
        String tokenType = jwtTokenProvider.getTokenType(jwt);

        if (redisUtil.checkWhiteList(tokenType, jti, userId)) {
            return next.handle(request);
        }

        throw new UnauthorizedException();
    }
}
