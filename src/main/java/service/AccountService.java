package service;

import exception.*;
import model.domain.LocalAccount;
import model.dto.AccountData;
import model.dto.TokenPair;
import model.dto.request.account.ChangePasswordRequest;
import model.dto.request.account.DeleteAccountRequest;
import model.dto.response.PKResponse;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import repository.AccountRepository;
import util.AccountUtil;
import util.JwtTokenProvider;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Optional;

public class AccountService extends BaseService {
    private JwtTokenProvider jwtTokenProvider;
    private AccountUtil accountUtil;
    private AccountRepository accountRepository;
    private JedisPool jedisPool;

//    public TokenPair createAccount(SignUpRequest request) {
//
//    }

    public TokenPair login(AccountData accountData) {
        try {
            LocalAccount localAccount = accountRepository.findId(accountData.getId())
                    .orElseThrow(AccountException::new);

            String salt = localAccount.getSalt();
            String savedPassword = localAccount.getPassword();

            if (accountUtil.matches(accountData.getPassword(), salt, savedPassword)) {
                String accessToken = jwtTokenProvider.createAccessToken(String.valueOf(localAccount.getPk()));
                String refreshToken = jwtTokenProvider.createRefreshToken(String.valueOf(localAccount.getPk()));

                try (Jedis jedis = jedisPool.getResource()) {
                    jedis.setex("refreshToken" + localAccount.getPk(), 3600, refreshToken);
                }

                return new TokenPair(accessToken, refreshToken);
            } else {
                throw new PasswordMisMatchException();
            }
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new InvalidException();
        }
    }

    public PKResponse changePassword(long userPk, ChangePasswordRequest request) {
        try {
            Optional<LocalAccount> result = accountRepository.findById(userPk);

            if (result.isPresent()) {
                String currentPassword = result.get().getPassword();
                String hashedInputPassword = accountUtil.hash(request.getCurrentPassword(), result.get().getSalt());

                if (currentPassword.equals(hashedInputPassword)) {
                    String newSalt = accountUtil.generateSalt();
                    String hashedPassword = accountUtil.hash(request.getNewPassword(), newSalt);

                    accountRepository.updatePassword(result.get().getPk(), hashedPassword, newSalt);

                    return new PKResponse(result.get().getPk());
                } else {
                    throw new PasswordMisMatchException();
                }
            } else {
                throw new NotFoundException();
            }
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new InvalidException(e.getMessage());
        }
    }

    public void logout(long userPk) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.del("refreshToken" + userPk);
        }
    }

    public void deleteAccount(long userPk, DeleteAccountRequest request) {
        try {
            Optional<LocalAccount> result = accountRepository.findById(userPk);

            if (result.isPresent()) {
                String hashedInputPassword = accountUtil.hash(request.getCheckPassword(), result.get().getSalt());

                if (hashedInputPassword.equals(result.get().getPassword())) {
                    accountRepository.delete(result.get().getPk());
                } else {
                    throw new PasswordMisMatchException();
                }
            } else {
                throw new NotFoundException();
            }
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new InvalidException(e.getMessage());
        }
    }
}
