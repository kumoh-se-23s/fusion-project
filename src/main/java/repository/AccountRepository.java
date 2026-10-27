package repository;

import model.domain.AppUser;
import model.domain.LocalAccount;
import model.dto.TokenPair;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.swing.text.html.Option;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AccountRepository extends BaseRepositoryImpl<LocalAccount, Long> {
    private final int ITERATIONS = 210_000;
    private final int KEY_LENGTH = 256;

    public Optional<TokenPair> login(String id, String password) {
        List<LocalAccount> resultList = find(Filter.eq("id", id).limit(1));

        Optional<LocalAccount> resultLocalAccount = resultList.stream().findFirst();

        if(resultLocalAccount.isPresent()) {
            String salt = resultLocalAccount.get().getSalt();

            if(salt + password == resultLocalAccount.get().getPassword()) {

            }



        } else {
            return Optional.empty();
        }
    }

    //Update 사용해도 상관없으나 salt를 받는 것을 명확하게 명시하기 위함
    public boolean changePassword(long accountPk, String hashedPassword, String salt) {
        return update(accountPk, Map.of(
                "password", hashedPassword,
                "salt", salt
        )).isPresent();
    }

    private String hash(String password, String salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
            PBEKeySpec spec = new PBEKeySpec(
                    password.toCharArray(),
                    Base64.getDecoder().decode(salt),
                    ITERATIONS,
                    KEY_LENGTH);

            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

            byte[] hashed = factory.generateSecret(spec).getEncoded();
    }

}
