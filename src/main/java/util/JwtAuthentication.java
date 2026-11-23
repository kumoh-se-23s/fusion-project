package util;

import com.minihttp.core.route.dependency.security.BearerSchema;
import config.AuthProperties;
import exception.InvalidTokenException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

public class JwtAuthentication extends BearerSchema {

    private final JwtTokenProvider jwtTokenProvider;
    private final RedisUtil redisUtil;

    public JwtAuthentication(JwtTokenProvider jwtTokenProvider, RedisUtil redisUtil) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.redisUtil = redisUtil;
    }

    final private SecretKey key = Keys.hmacShaKeyFor(
            AuthProperties.get("jwt.secret")
                    .getBytes(StandardCharsets.UTF_8));

    @Override
    protected Object authenticate(String token) {
        Claims claims = getClaims(token);

        if (claims.getSubject() == null || claims.getSubject().isEmpty()) {
            throw new InvalidTokenException();
        }

        if (claims.getId() == null || claims.getId().isEmpty()) {
            throw new InvalidTokenException();
        }

        if (!"access".equals(claims.get("type", String.class)) && !"refresh".equals(claims.get("type", String.class))) {
            throw new InvalidTokenException();
        }

        String tokenType = jwtTokenProvider.getTokenType(token);
        String jti = jwtTokenProvider.getJti(token);
        long userId = jwtTokenProvider.getUserId(token);

        if (!redisUtil.isRegisteredInRedis(tokenType, jti, userId)) {
            throw new InvalidTokenException();
        }

        return userId;
    }

    private Claims getClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            throw new InvalidTokenException();
        }
    }
}
