package repository;

import model.domain.AppUser;
import model.domain.LocalAccount;
import model.dto.TokenPair;

import java.util.Map;
import java.util.Optional;

public class AccountRepository extends BaseRepositoryImpl<LocalAccount, Long> {
    //Update 사용해도 상관없으나 salt를 받는 것을 명확하게 명시하기 위함
    public boolean changePassword(long accountPk, String hashedPassword, String salt) {
        return update(accountPk, Map.of(
                "password", hashedPassword,
                "salt", salt
        )).isPresent();
    }

}
