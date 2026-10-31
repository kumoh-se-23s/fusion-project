package model.dto;

import model.Serializer;
import model.domain.AppUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UserData <-> AppUser 의 {@link Serializer} 바인딩 테스트.
 * 바인딩 설정이 잘못되면 첫 변환 때 SerializeException 이 나므로, 여기서 미리 잡는다.
 */
class UserDataTest {

    private static AppUser user() {
        return AppUser.builder()
                .pk(1L)
                .name("홍길동")
                .email("hong@example.com")
                .postal("12345")
                .address("서울시")
                .phone("010-1234-5678")
                .build();
    }

    @Test
    @DisplayName("AppUser -> UserData 변환 시 모든 필드가 옮겨진다")
    void toData() {
        UserData data = Serializer.toData(user(), UserData.class);

        assertEquals("홍길동", data.getName());
        assertEquals("hong@example.com", data.getEmail());
        assertEquals("12345", data.getPostal());
        assertEquals("서울시", data.getAddress());
        assertEquals("010-1234-5678", data.getPhone());
    }

    @Test
    @DisplayName("UserData -> AppUser 변환 시 모든 필드가 옮겨진다")
    void toDomain() {
        UserData data = new UserData("홍길동", "hong@example.com", "서울시", "12345", "010-1234-5678");

        AppUser user = Serializer.toDomain(data, AppUser.class);

        assertEquals("홍길동", user.getName());
        assertEquals("hong@example.com", user.getEmail());
        assertEquals("12345", user.getPostal());
        assertEquals("서울시", user.getAddress());
        assertEquals("010-1234-5678", user.getPhone());
    }
}
