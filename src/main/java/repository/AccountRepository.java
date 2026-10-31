package repository;

import model.domain.LocalAccount;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AccountRepository extends BaseRepositoryImpl<LocalAccount, Long> {

//    CRUD에 맞는 네이밍
//    id는 UNIQUE

    public Optional<LocalAccount> findId(String id) {

        List<LocalAccount> result = find(
                Filter.builder()
                        .conditions(Map.of("id", id))
                        .limit(1)
                        .build()
        );

        if (result.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(result.getFirst());
        }
    }

    //파라미터로 다 받을지 VS 안 받을지(DTO)
    //회원가입 시 사용 예정, salt,user => NonNull이므로 salt 생성 user 받아야함
    public void createAccount(String id, String password) {
        LocalAccount localAccount = LocalAccount.builder()
                .id(id)
                .password(password)
                .build();

        create(localAccount);
    }

    //Update 사용해도 상관없으나 salt를 받는 것을 명확하게 명시하기 위함
    public boolean updatePassword(long accountPk, String hashedPassword, String salt) {
        return update(accountPk, Map.of(
                "password", hashedPassword,
                "salt", salt
        )).isPresent();
    }
}
