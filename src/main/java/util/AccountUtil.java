package util;

import com.mysql.cj.util.Base64Decoder;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

public class AccountUtil {

    private final int ITERATIONS = 210_000;
    private final int KEY_LENGTH = 256;
    private final int SALT_LENGTH = 16;
    private final SecureRandom random = new SecureRandom();

    public String hash(String password, String salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
        PBEKeySpec spec = new PBEKeySpec(
                password.toCharArray(),
                Base64.getDecoder().decode(salt),
                ITERATIONS,
                KEY_LENGTH);

        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

        byte[] hashed = factory.generateSecret(spec).getEncoded();
        spec.clearPassword();

        return Base64.getEncoder().encodeToString(hashed);
    }

    public boolean matches(String inputPassword, String salt, String savedPassword) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] input = Base64.getDecoder().decode(hash(inputPassword, salt));
        byte[] saved = Base64.getDecoder().decode(savedPassword);
        return MessageDigest.isEqual(input, saved);
    }

    public String generateSalt() {
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }
}
