package fixture;

import constant.Role;
import constant.SocialProvider;
import model.domain.AppUser;
import model.domain.LocalAccount;
import model.domain.SocialAccount;
import model.domain.UserRole;
import model.domain.UserRoleRelation;
import repository.LocalAccountRepository;
import repository.SocialAccountRepository;
import repository.UserRoleRelationRepository;
import repository.UserRoleRepository;
import support.TestServer;
import util.AccountUtil;

import java.util.UUID;

/**
 * 계정 관련 테스트 데이터(로컬 계정, 소셜 계정, 권한)를 DB에 만든다.
 * 서버와 같은 리포지토리 인스턴스를 쓴다.
 */
public final class AccountFixtures {
    private AccountFixtures() {
    }

    private static <T> T bean(Class<T> type) {
        return TestServer.get().bean(type);
    }

    /** 로그인 아이디와 평문 비밀번호로 로컬 계정을 만든다. 비밀번호는 회원가입과 같은 방식(salt + 해시)으로 저장한다. */
    public static LocalAccount createLocalAccount(AppUser user, String loginId, String rawPassword) {
        AccountUtil accountUtil = bean(AccountUtil.class);
        LocalAccountRepository repository = bean(LocalAccountRepository.class);
        String salt = accountUtil.generateSalt();
        try {
            repository.createLocalAccount(user, loginId, accountUtil.hash(rawPassword, salt), salt);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        return repository.findByLoginId(loginId).orElseThrow();
    }

    /** 권한 데이터가 없으면 만든다. (테이블은 JVM 시작 때 비어 있다) */
    public static synchronized UserRole ensureRole(Role role) {
        UserRoleRepository repository = bean(UserRoleRepository.class);
        return repository.findByRole(role).orElseGet(() -> {
            try {
                return repository.create(UserRole.builder().role(role).build());
            } catch (RuntimeException e) {
                //다른 테스트가 먼저 만들었으면 그걸 쓴다
                return repository.findByRole(role).orElseThrow(() -> e);
            }
        });
    }

    /** 유저에게 권한을 준다. */
    public static UserRoleRelation grantRole(AppUser user, Role role) {
        return bean(UserRoleRelationRepository.class).create(UserRoleRelation.builder()
                .user(user)
                .role(ensureRole(role))
                .build());
    }

    /** 소셜 계정을 연동된 상태로 만든다. 제공자 고유 ID는 겹치지 않게 만든다. */
    public static SocialAccount createSocialAccount(AppUser user, SocialProvider provider) {
        return bean(SocialAccountRepository.class)
                .createSocialAccount(uniqueProviderUserId(), user, provider);
    }

    public static String uniqueProviderUserId() {
        return "provider-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
}
