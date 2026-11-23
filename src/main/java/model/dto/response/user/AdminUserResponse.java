package model.dto.response.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import model.dto.response.Response;

import java.time.LocalDateTime;

/**
 * 유저 단건 조회 (GET /internal/user/{user_id}) 응답, 유저 전체 조회의 목록 한 칸 <br>
 * API 명세서의 Response Body 필드만 담는다. (엔티티의 version 같은 내부 값은 내보내지 않는다)
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminUserResponse extends Response {
    private Long userId;

    private String name;

    private String nickname;

    private String email;

    private String postalCode;

    private String address;

    private String phone;

    private LocalDateTime createdAt;
}
