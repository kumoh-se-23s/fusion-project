package service;

import annotation.PK;
import exception.AccountException;
import exception.BusinessException;
import model.domain.LocalAccount;
import model.dto.TokenPair;
import model.dto.request.account.ChangePasswordRequest;
import model.dto.request.account.DeleteAccountRequest;
import model.dto.request.user.SignUpRequest;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import repository.AccountRepository;
import util.AccountUtil;
import util.JwtTokenProvider;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;
import java.util.Optional;

public class AccountService {
    private JwtTokenProvider jwtTokenProvider;
    private AccountUtil accountUtil;

    private AccountRepository accountRepository;
    private JedisPool jedisPool;

    public TokenPair signUp(SignUpRequest request) {

    }

    public Optional<TokenPair> login(String id, String inputPassword) {

        try {
            Optional<LocalAccount> checkIdResult = accountRepository.checkID(id);

            if (checkIdResult.isEmpty()) {
                throw new AccountException();
            }

            LocalAccount localAccount = checkIdResult.get();

            String salt = localAccount.getSalt();
            String savedPassword = localAccount.getPassword();

            if (accountUtil.matches(inputPassword, salt, savedPassword)) {
                String accessToken = jwtTokenProvider.createAccessToken(String.valueOf(localAccount.getPk()));
                String refreshToken = jwtTokenProvider.createRefreshToken(String.valueOf(localAccount.getPk()));

                try (Jedis jedis = jedisPool.getResource()) {
                    jedis.setex("refreshToken" + localAccount.getPk(), 3600 , refreshToken);
                }

                return Optional.of(new TokenPair(accessToken, refreshToken));
            }
        } catch () {

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException(e);
        } catch (BusinessException e) {

        }
    }

    public void changePassword(long userPk, ChangePasswordRequest request) {
        try {
            Optional<LocalAccount> result = accountRepository.findByUserPk(userPk);

            if (result.isPresent()) {
                String currentPassword = result.get().getPassword();
                String hashedInputPassword = accountUtil.hash(request.getCurrentPassword(),result.get().getSalt());

                if (currentPassword.equals(hashedInputPassword)) {
                    String newSalt = accountUtil.generateSalt();
                    String hashedPassword = accountUtil.hash(request.getNewPassword(), newSalt);

                    accountRepository.updatePassword(result.get().getPk(), hashedPassword, newSalt);
                } else {
                    throw //다른 비밀번호임
                }
            } else {
                throw //존재하지 않는 pk
            }


        } catch (NoSuchAlgorithmException e) {

        } catch (InvalidKeySpecException e) {

        }
    }

    public void logout(long userPk) {
        try(Jedis jedis = jedisPool.getResource()) {
            jedis.del("refreshToken" + userPk);
        }
    }

    public void deleteAccount(long userPk, DeleteAccountRequest request) {
        try {
            Optional<LocalAccount> result = accountRepository.findByUserPk(userPk);

            if(result.isPresent()) {
                String hashedInputPassword = accountUtil.hash(request.getCheckPassword(), result.get().getSalt());

                if(hashedInputPassword.equals(result.get().getPassword())) {
                    accountRepository.delete(result.get().getPk());
                } else {
                    throw //
                }
            } else {
                throw //
            }
        } catch (NoSuchAlgorithmException e) {

        } catch (InvalidKeySpecException e) {

        }
     }
}
