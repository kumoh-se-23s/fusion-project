package repository;

import constant.SocialProvider;
import model.domain.AppUser;
import model.domain.SocialAccount;

public class SocialAccountRepository extends  BaseRepositoryImpl<SocialAccount, Long> {
    public void createSocialAccount(Long id, AppUser appUser, SocialProvider socialProvider) {
        SocialAccount socialAccount = SocialAccount.builder()
                .appUser(appUser)
                .socialProvider(socialProvider)
                .build();

        create(socialAccount);
    }
}
