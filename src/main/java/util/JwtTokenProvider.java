package util;

import com.fasterxml.jackson.databind.ObjectMapper;
import exception.TokenExpiredException;
import exception.UnauthorizedException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;



public class JwtTokenProvider {
    final private ObjectMapper objectMapper = new ObjectMapper();

    final private String alg = "HS256";

    //비밀 키 위치 옮겨야함
    final private String secretKey = "YEm5Guw205YIM4QuSKDrQqv/nmvssJwbQQc8OPCJ4lM=";
    final private SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));


    public String createAccessToken(long userId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + 1000 * 60 * 15);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .id(UUID.randomUUID().toString())
                .claim("type", "access")
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    public String createRefreshToken(long userId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + 1000 * 60 * 60);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .id(UUID.randomUUID().toString())
                .claim("type", "refresh")
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }


    private Claims getClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw new TokenExpiredException();
        } catch (JwtException | IllegalArgumentException e) {
            throw new UnauthorizedException();
        }
    }

    public boolean validateToken(String token) throws JwtException, IllegalArgumentException {

        Claims claims = getClaims(token);

        if (claims.getSubject() == null || claims.getSubject().isEmpty()) {
            return false;
        }

        if (claims.getId() == null || claims.getId().isEmpty()) {
            return false;
        }

        if (!"access".equals(claims.get("type", String.class))) {
            return false;
        }

        return true;
    }

    public long getUserId(String token) {
        String stringTypeUserId = getClaims(token).getSubject();

        return Long.parseLong(stringTypeUserId);
    }

    public String getJti(String token) {
        return getClaims(token).getId();
    }

    public String getTokenType(String token) throws JwtException, IllegalArgumentException {
        Claims claims = getClaims(token);
        return claims.get("type", String.class);
    }
}