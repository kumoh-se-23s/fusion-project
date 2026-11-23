package model.dto;

import constant.SocialProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SocialUserInfo {

    //소셜 제공자가 알려 주는 회원 고유 ID (구글 sub, 카카오 id, 네이버 response.id). 이메일은 바뀔 수 있어 이 값으로 계정을 구분한다
    private String providerUserId;
    private String email;
    private String name;

    //어떤 소셜 제공자로 가입하는지. 콜백에서 회원가입 키와 함께 Redis에 저장할 때만 채운다 (OAuthClient는 비워 둠)
    private SocialProvider socialProvider;
}
