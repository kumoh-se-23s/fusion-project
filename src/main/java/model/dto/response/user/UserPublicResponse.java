package model.dto.response.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import model.dto.response.Response;

/**
 * 유저 정보 조회 (GET /user/{user_id}) 응답 <br>
 * 다른 유저에게 보여 줘도 되는 공개 정보(id, 닉네임)만 담는다. 이메일, 전화번호, 주소 같은 개인정보는 넣지 않는다
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserPublicResponse extends Response {
    private Long userId;

    private String nickname;
}
