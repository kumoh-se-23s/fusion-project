package util;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private SecretKey key;

    @BeforeEach
    void setUp() throws Exception {
        jwtTokenProvider = new JwtTokenProvider();

        // JwtTokenProvider 내부의 비밀 키를 꺼내서 테스트용 토큰을 직접 만들 때 사용
        Field keyField = JwtTokenProvider.class.getDeclaredField("key");
        keyField.setAccessible(true);
        key = (SecretKey) keyField.get(jwtTokenProvider);
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    @Nested
    @DisplayName("createAccessToken")
    class CreateAccessToken {

        @Test
        @DisplayName("subject에 userPK가 들어간다")
        void subjectIsUserPK() {
            String token = jwtTokenProvider.createAccessToken("1");

            assertEquals("1", parseClaims(token).getSubject());
        }

        @Test
        @DisplayName("만료 시간은 발급 시간으로부터 15분 뒤다")
        void expiresIn15Minutes() {
            String token = jwtTokenProvider.createAccessToken("1");
            Claims claims = parseClaims(token);

            long diffMillis = claims.getExpiration().getTime() - claims.getIssuedAt().getTime();
            assertEquals(1000 * 60 * 15, diffMillis);
        }

        @Test
        @DisplayName("토큰은 header.payload.signature 세 부분으로 되어 있다")
        void hasThreeParts() {
            String token = jwtTokenProvider.createAccessToken("1");

            assertEquals(3, token.split("\\.").length);
        }
    }

    @Nested
    @DisplayName("createRefreshToken")
    class CreateRefreshToken {

        @Test
        @DisplayName("subject에 userPK가 들어간다")
        void subjectIsUserPK() {
            String token = jwtTokenProvider.createRefreshToken("42");

            assertEquals("42", parseClaims(token).getSubject());
        }

        @Test
        @DisplayName("만료 시간이 설정되어 있고 현재보다 미래다")
        void hasFutureExpiration() {
            String token = jwtTokenProvider.createRefreshToken("42");

            assertTrue(parseClaims(token).getExpiration().after(new Date()));
        }
    }

    @Nested
    @DisplayName("validateToken")
    class ValidateToken {

        @Test
        @DisplayName("정상 access token은 true")
        void validAccessToken() throws JsonProcessingException {
            String token = jwtTokenProvider.createAccessToken("1");

            assertTrue(jwtTokenProvider.validateToken(token));
        }

        @Test
        @DisplayName("정상 refresh token은 true")
        void validRefreshToken() throws JsonProcessingException {
            String token = jwtTokenProvider.createRefreshToken("1");

            assertTrue(jwtTokenProvider.validateToken(token));
        }

        @Test
        @DisplayName("만료된 토큰은 false")
        void expiredToken() throws JsonProcessingException {
            Date past = new Date(System.currentTimeMillis() - 1000 * 60);
            String token = Jwts.builder()
                    .subject("1")
                    .issuedAt(new Date(past.getTime() - 1000 * 60))
                    .expiration(past)
                    .signWith(key)
                    .compact();

            assertFalse(jwtTokenProvider.validateToken(token));
        }

        @Test
        @DisplayName("subject가 없는 토큰은 false")
        void noSubject() throws JsonProcessingException {
            String token = Jwts.builder()
                    .issuedAt(new Date())
                    .expiration(new Date(System.currentTimeMillis() + 1000 * 60))
                    .signWith(key)
                    .compact();

            assertFalse(jwtTokenProvider.validateToken(token));
        }

        @Test
        @DisplayName("다른 키로 서명한 토큰은 false")
        void signedWithOtherKey() throws JsonProcessingException {
            SecretKey otherKey = Keys.hmacShaKeyFor(
                    "another-secret-key-for-test-1234".getBytes(StandardCharsets.UTF_8));
            String token = Jwts.builder()
                    .subject("1")
                    .issuedAt(new Date())
                    .expiration(new Date(System.currentTimeMillis() + 1000 * 60))
                    .signWith(otherKey)
                    .compact();

            assertFalse(jwtTokenProvider.validateToken(token));
        }

        @Test
        @DisplayName("HS256이 아닌 알고리즘(HS512) 토큰은 false")
        void otherAlgorithm() throws JsonProcessingException {
            SecretKey hs512Key = Jwts.SIG.HS512.key().build();
            String token = Jwts.builder()
                    .subject("1")
                    .issuedAt(new Date())
                    .expiration(new Date(System.currentTimeMillis() + 1000 * 60))
                    .signWith(hs512Key)
                    .compact();

            assertFalse(jwtTokenProvider.validateToken(token));
        }

        @Test
        @DisplayName("payload를 바꿔치기한 토큰은 false")
        void tamperedPayload() throws JsonProcessingException {
            String token = jwtTokenProvider.createAccessToken("1");
            String[] parts = token.split("\\.");

            String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            String tamperedPayload = payload.replace("\"sub\":\"1\"", "\"sub\":\"999\"");
            String encoded = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(tamperedPayload.getBytes(StandardCharsets.UTF_8));

            String tamperedToken = parts[0] + "." + encoded + "." + parts[2];

            assertFalse(jwtTokenProvider.validateToken(tamperedToken));
        }

        @Test
        @DisplayName("signature를 바꿔치기한 토큰은 false")
        void tamperedSignature() throws JsonProcessingException {
            String token = jwtTokenProvider.createAccessToken("1");
            String[] parts = token.split("\\.");

            String tamperedToken = parts[0] + "." + parts[1] + "." + "invalidSignature";

            assertFalse(jwtTokenProvider.validateToken(tamperedToken));
        }

        // 아래 두 테스트는 "현재 동작"을 기록한다.
        // 잘못된 형식의 토큰이 들어오면 false가 아니라 예외가 터진다.
        @Test
        @DisplayName("[현재 동작] 점(.)이 없는 토큰은 예외가 발생한다")
        void malformedToken() {
            assertThrows(ArrayIndexOutOfBoundsException.class,
                    () -> jwtTokenProvider.validateToken("not-a-jwt"));
        }

        @Test
        @DisplayName("[현재 동작] exp가 없는 토큰은 RuntimeException이 발생한다")
        void noExpiration() {
            String token = Jwts.builder()
                    .subject("1")
                    .issuedAt(new Date())
                    .signWith(key)
                    .compact();

            assertThrows(RuntimeException.class, () -> jwtTokenProvider.validateToken(token));
        }
    }
}
