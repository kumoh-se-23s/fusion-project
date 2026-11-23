package model.dto.request.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import model.dto.request.Request;

/**
 * 소셜 회원가입 (POST /account/social-signup?key={key}) 요청 <br>
 * 이름, 이메일은 소셜 제공자가 주므로, 소셜 계정으로 받을 수 없는 값만 받는다
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateSocialUserRequest extends Request {
    //DB에서 필수(nullable = false)라 꼭 받아야 한다
    private String nickname;

    private String address;

    //내 정보 조회(MyInfoResponse)와 같은 이름. AppUser에서는 postal 필드다
    private String postalCode;

    private String phone;
}
