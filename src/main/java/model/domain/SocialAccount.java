package model.domain;

import annotation.*;
import constant.SocialProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "SOCIAL_ACCOUNT")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SocialAccount extends BaseDomain {

    @Id
    @Column(name = "social_account_id")
    private Long id;

    //한 유저가 여러 소셜 계정(구글, 카카오 등)을 연동할 수 있으므로 unique 아님
    @JoinColumn(name = "user_fk", nullable = false, onDelete = OnDelete.CASCADE)
    private AppUser appUser;

    //소셜 제공자 (GOOGLE / KAKAO / NAVER). enum 이름으로 저장된다
    @Column(name = "social_provider", nullable = false, length = 20, uniqueGroup = "social_identity")
    private SocialProvider socialProvider;

    //소셜 제공자가 알려 주는 회원 고유 ID. (제공자 + 고유 ID) 조합은 하나뿐이다
    @Column(name = "social_provider_user_id", nullable = false, uniqueGroup = "social_identity")
    private String socialProviderUserId;
}
