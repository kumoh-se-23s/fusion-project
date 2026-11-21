package model.dto;

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
}
