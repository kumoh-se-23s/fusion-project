package repository;

import constant.SocialProvider;
import model.domain.AppUser;
import model.domain.SocialAccount;

public class SocialAccountRepository extends  BaseRepositoryImpl<SocialAccount, Long> {
    public void createSocialAccount(String id, AppUser appUser, SocialProvider socialProvider) {
        SocialAccount socialAccount = SocialAccount.builder()
                .id(id)
                .appUser(appUser)
                .socialProvider(socialProvider)
                .build();

        create(socialAccount);
    }
}
