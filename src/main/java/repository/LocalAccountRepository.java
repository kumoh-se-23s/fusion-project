package repository;

import model.domain.LocalAccount;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class LocalAccountRepository extends BaseRepositoryImpl<LocalAccount, Long> {

//    CRUD에 맞는 네이밍
//    loginId는 UNIQUE

    public void createLocalAccount(String loginId, String password) {
        LocalAccount localAccount = LocalAccount.builder()
                .loginId(loginId)
                .password(password)
                .build();

        create(localAccount);
    }

    public Optional<LocalAccount> findByLoginId(String loginId) {

        List<LocalAccount> result = find(
                Filter.builder()
                        .conditions(Map.of("loginId", loginId))
                        .limit(1)
                        .build()
        );

        if (result.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(result.get(0));
        }
    }

    //파라미터로 다 받을지 VS 안 받을지(DTO)
    //회원가입 시 사용 예정, salt,user => NonNull이므로 salt 생성 user 받아야함

    //Update 사용해도 상관없으나 salt를 받는 것을 명확하게 명시하기 위함
    public boolean updatePassword(long accountId, String hashedPassword, String salt) {
        return update(accountId, Map.of(
                "password", hashedPassword,
                "salt", salt
        )).isPresent();
    }
}
