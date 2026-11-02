package util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    public String createAccessToken(String userId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + 1000 * 60 * 15);

        return Jwts.builder()
                .subject(userId)
                .claim("type", "access")
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    public String createRefreshToken(String userId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + 1000 * 60 * 15);

        return Jwts.builder()
                .subject(userId)
                .claim("type" , "refresh")
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    //서명, 만료시간, 형식을 한번에 확인해주고 맞으면 Claims를 넘김
    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token) throws JwtException, IllegalArgumentException {
        return getClaims(token).getSubject() != null;
    }

    public boolean isAccessToken(Claims claims) throws JwtException, IllegalArgumentException {
        String value = claims.get("type", String.class);
        return "access".equals(value);
    }
}