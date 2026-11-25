package auth;

import com.minihttp.core.route.dependency.security.BearerSchema;
import constant.RedisKey;
import exception.UnauthorizedException;
import util.JwtTokenProvider;
import util.RedisUtil;

public class CurrentUser extends BearerSchema<Long> {
    private final JwtTokenProvider tokenProvider = new JwtTokenProvider();
    private final RedisUtil redisUtil = new RedisUtil();

    @Override
    protected Long authenticate(String token) {
        if (!this.tokenProvider.validateToken(token)) {
            throw new UnauthorizedException();
        }

        String tokenType = tokenProvider.getTokenType(token);
        if (!tokenType.equals("access")) {
            throw new UnauthorizedException();
        }

        String jti = tokenProvider.getJti(token);
        long userId = tokenProvider.getUserId(token);
        if (!redisUtil.isRegisteredInRedis(tokenType, jti, userId)) {
            throw new UnauthorizedException();
        }

        return tokenProvider.getUserId(token);
    }
}
