package repository;

import constant.SocialProvider;
import model.domain.AppUser;
import model.domain.SocialAccount;

import java.util.List;
import java.util.Map;

public class SocialAccountRepository extends  BaseRepositoryImpl<SocialAccount, Long> {
    //id(social_account_id)는 DB가 자동으로 만든다. 저장된 엔티티(생성된 id 포함)를 돌려준다
    public SocialAccount createSocialAccount(String socialProviderUserId, AppUser appUser, SocialProvider socialProvider) {
        SocialAccount socialAccount = SocialAccount.builder()
                .appUser(appUser)
                .socialProvider(socialProvider)
                .socialProviderUserId(socialProviderUserId)
                .build();

        return create(socialAccount);
    }

    //(제공자 + 고유 ID) 소셜 계정이 이미 누군가에게 연동되어 있는지
    public boolean isExistSocialIdentity(SocialProvider socialProvider, String socialProviderUserId) {
        return isExist(Filter.builder()
                .conditions(Map.of(
                        "socialProvider", socialProvider,
                        "socialProviderUserId", socialProviderUserId))
                .build());
    }

    //이 유저가 같은 제공자의 소셜 계정을 이미 연동했는지
    public boolean isExistByUserAndProvider(long userId, SocialProvider socialProvider) {
        return isExist(Filter.builder()
                .conditions(Map.of(
                        "appUser", userId,
                        "socialProvider", socialProvider))
                .build());
    }

    //이 유저에게 연동된 소셜 계정 전부. 한 유저가 여러 개를 가질 수 있으므로 List
    //Filter의 키는 DB 컬럼명(user_fk)이 아니라 도메인 필드명(appUser)이다
    public List<SocialAccount> findByUserId(long userId) {
        return find(Filter.builder()
                .conditions(Map.of("appUser", userId))
                .build());
    }
}
