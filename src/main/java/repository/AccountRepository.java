package repository;

import model.domain.LocalAccount;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AccountRepository extends BaseRepositoryImpl<LocalAccount, Long> {

    public Optional<LocalAccount> checkID(String id) {
        List<LocalAccount> result = find(Filter.eq("id", id).limit(1));

        if(result.isEmpty()){
            return Optional.empty();
        } else {
            return Optional.of(find(Filter.eq("id", id).limit(1)).getFirst());
        }
    }

    public void signUp(String id, String password) {
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

    public Optional<LocalAccount> findByUserPk(long userPk) {
        return findById(userPk);
    }
}
