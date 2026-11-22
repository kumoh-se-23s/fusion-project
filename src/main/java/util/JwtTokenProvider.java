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
    //토큰 유효 시간(초). JWT 만료 시간과 Redis 저장 시간이 항상 같도록 여기 한 곳에서만 정한다
    public static final int ACCESS_TOKEN_EXPIRE_SECONDS = 60 * 30;             //30분
    public static final int REFRESH_TOKEN_EXPIRE_SECONDS = 60 * 60 * 24 * 14;  //14일

    final private ObjectMapper objectMapper = new ObjectMapper();

    final private String alg = "HS256";

    //비밀 키 위치 옮겨야함
    final private String secretKey = "YEm5Guw205YIM4QuSKDrQqv/nmvssJwbQQc8OPCJ4lM=";
    final private SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));


    public String createAccessToken(long userId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + 1000L * ACCESS_TOKEN_EXPIRE_SECONDS);

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
        Date expiry = new Date(now.getTime() + 1000L * REFRESH_TOKEN_EXPIRE_SECONDS);

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

        //access도 아니고 refresh도 아닐 때만 거절 (|| 로 쓰면 어떤 토큰이든 항상 false가 된다)
        if (!"access".equals(claims.get("type", String.class)) && !"refresh".equals(claims.get("type", String.class))) {
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