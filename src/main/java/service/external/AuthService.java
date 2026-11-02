package service.external;

import com.sun.jdi.request.InvalidRequestStateException;
import exception.*;
import model.domain.AppUser;
import model.domain.LocalAccount;
import model.dto.AccountData;
import model.dto.TokenPair;
import model.dto.request.account.ChangePasswordRequest;
import model.dto.request.account.CreateLocalAccountRequest;
import model.dto.request.account.DeleteAccountRequest;
import model.dto.request.user.CreateUserRequest;
import model.dto.response.IdResponse;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import repository.AccountRepository;
import repository.UserRepository;
import service.BaseService;
import util.AccountUtil;
import util.JwtTokenProvider;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class AuthService extends BaseService {
    private final JwtTokenProvider jwtTokenProvider;
    private final AccountUtil accountUtil;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    private final JedisPool jedisPool;

    public AuthService(JwtTokenProvider jwtTokenProvider, AccountUtil accountUtil, AccountRepository accountRepository, UserRepository userRepository, JedisPool jedisPool) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.accountUtil = accountUtil;
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.jedisPool = jedisPool;
    }

    public TokenPair signUp(CreateLocalAccountRequest accountRequest, CreateUserRequest userRequest) {
        if(userRepository.isExistEmail(userRequest.getUserData().getEmail())) {
            throw new DuplicateEmailException();
        } else {
            return tx.execute(() -> {
                accountRepository.createAccount(accountRequest.getId(), accountRequest.getPassword());
                AppUser appuser = userRepository.createUser(userRequest.getUserData()).orElseThrow(InvalidRequestStateException::new);

                String accessToken = jwtTokenProvider.createAccessToken(String.valueOf(appuser.getId()));
                String refreshToken = jwtTokenProvider.createRefreshToken(String.valueOf(appuser.getId()));

                return new TokenPair(accessToken, refreshToken);
            });
        }
    }

    public TokenPair login(AccountData accountData) {
        try {
            LocalAccount localAccount = accountRepository.findByLoginId(accountData.getLoginId())
                    .orElseThrow(AccountException::new);

            String salt = localAccount.getSalt();
            String savedPassword = localAccount.getPassword();

            if (accountUtil.matches(accountData.getPassword(), salt, savedPassword)) {
                String accessToken = jwtTokenProvider.createAccessToken(String.valueOf(localAccount.getId()));
                String refreshToken = jwtTokenProvider.createRefreshToken(String.valueOf(localAccount.getId()));

                try (Jedis jedis = jedisPool.getResource()) {
                    jedis.setex("refreshToken" + localAccount.getId(), 3600, refreshToken);
                }

                return new TokenPair(accessToken, refreshToken);
            } else {
                throw new PasswordMisMatchException();
            }
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new InvalidException();
        }
    }

    public IdResponse changePassword(long userId, ChangePasswordRequest request) {
        try {
            // account repository에서 user Id로 pk 조회를 하면 어캄?...
            LocalAccount result = accountRepository.findById(userId).orElseThrow(NotFoundException::new);

            String currentPassword = result.getPassword();
            String hashedInputPassword = accountUtil.hash(request.getCurrentPassword(), result.getSalt());

            if (currentPassword.equals(hashedInputPassword)) {
                String newSalt = accountUtil.generateSalt();
                String hashedPassword = accountUtil.hash(request.getNewPassword(), newSalt);

                accountRepository.updatePassword(result.getId(), hashedPassword, newSalt);

                return new IdResponse(result.getId());
            } else {
                throw new PasswordMisMatchException();
            }

        } catch (NoSuchAlgorithmException |
                 InvalidKeySpecException e) {
            throw new InvalidException(e.getMessage());
        }
    }

    public void logout(long userId) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.del("refreshToken" + userId);
        }
    }

    public void deleteAccount(long userId, DeleteAccountRequest request) {
        try {
            // account repository에서 user Id로 pk 조회를 하면 어캄?...
            LocalAccount result = accountRepository.findById(userId).orElseThrow(NotFoundException::new);

            String hashedInputPassword = accountUtil.hash(request.getCheckPassword(), result.getSalt());

            if (hashedInputPassword.equals(result.getPassword())) {
                accountRepository.delete(result.getId());
            } else {
                throw new PasswordMisMatchException();
            }

        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new InvalidException(e.getMessage());
        }
    }
}
