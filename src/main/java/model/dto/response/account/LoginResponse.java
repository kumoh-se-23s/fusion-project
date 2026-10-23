package model.dto.response.account;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
    private String accessToken;

    private String refreshToken;
}


/*
로컬로그인 -> request, response
로컬 유저 비밀번호 변경 -> request, response
계정 삭제 -> request, response

로컬 유저 회원가입 -> request, response
유저 정보 수정 -> request,response
개인 유저 정보 조회 -> request, response

 */