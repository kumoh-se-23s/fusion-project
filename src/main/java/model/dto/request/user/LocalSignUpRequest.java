package model.dto.request.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import model.dto.request.Request;

/**
 * 로컬 유저 회원가입 (POST /user/local-register) 요청 <br>
 * API 명세서의 Request Body처럼 계정 정보(loginId, password)와 유저 정보를 한 번에 받는다
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LocalSignUpRequest extends Request {
    private String loginId;

    private String password;

    private String name;

    private String nickname;

    private String email;

    //내 정보 조회(MyInfoResponse)와 같은 이름. AppUser에서는 postal 필드다
    private String postalCode;

    private String address;

    private String phone;
}
