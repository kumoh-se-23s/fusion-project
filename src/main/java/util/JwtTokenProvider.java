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

    public String createAccessToken(String userID) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + 1000 * 60 * 15);

        return Jwts.builder()
                .subject(userID)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    public String createRefreshToken(String userID) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + 1000 * 60 * 15);

        return Jwts.builder()
                .subject(userID)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Map<String, String> parseToken = parse(token);

            String header = parseToken.get("header");
            Map<String, Object> decodedHeader = jsonToMap(decoding(header));

            if (!decodedHeader.get("alg").equals(alg)) {
                return false;
            }

            String payload = parseToken.get("payload");
            Map<String, Object> decodedPayload = jsonToMap(decoding(payload));

            long now = System.currentTimeMillis() / 1000;
            if (((Number) decodedPayload.get("exp")).longValue() < now || decodedPayload.get("sub") == null) {
                return false;
            }

            String expectedToken = Jwts.builder()
                    .header()
                    .add("alg", alg)
                    .and()
                    .content(decoding(payload))
                    .signWith(key)
                    .compact();

            if (!expectedToken.equals(token)) {
                return false;
            }

            return true;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } catch (NullPointerException e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, String> parse(String token) {
        String[] splitTokens = token.split("\\.");

        Map<String, String> result = new HashMap<String, String>();

        result.put("header", splitTokens[0]);
        result.put("payload", splitTokens[1]);
        result.put("signature", splitTokens[2]);

        return result;
    }

    private String decoding(String token) {
        return new String(Base64.getUrlDecoder().decode(token));
    }

    private Map<String, Object> jsonToMap(String json) throws JsonProcessingException {
        return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {
        });
    }
}