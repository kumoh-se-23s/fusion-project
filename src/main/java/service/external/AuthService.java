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
import model.dto.request.user.CreateSocialUserRequest;
import model.dto.request.user.CreateUserRequest;
import model.dto.response.IdResponse;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import repository.LocalAccountRepository;
import repository.UserRepository;
import service.BaseService;
import util.AccountUtil;
import util.JwtTokenProvider;
import util.RedisUtil;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class AuthService extends BaseService {
    private final JwtTokenProvider jwtTokenProvider;
    private final AccountUtil accountUtil;
    private final LocalAccountRepository localAccountRepository;
    private final UserRepository userRepository;

    private final RedisUtil redisUtil;

    public AuthService(JwtTokenProvider jwtTokenProvider, AccountUtil accountUtil, LocalAccountRepository localAccountRepository, UserRepository userRepository, RedisUtil redisUtil) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.accountUtil = accountUtil;
        this.localAccountRepository = localAccountRepository;
        this.userRepository = userRepository;
        this.redisUtil = redisUtil;
    }

    public TokenPair localSignUp(CreateLocalAccountRequest accountRequest, CreateUserRequest userRequest) {
        return tx.execute(() -> {
            if (userRepository.isExistEmail(userRequest.getUserData().getEmail())) {
                throw new DuplicateEmailException();
            }

            if (localAccountRepository.isExistLoginId(accountRequest.getId())) {
                throw new DuplicateIdException();
            }

            String salt = accountUtil.generateSalt();

            AppUser appuser = userRepository.createUser(userRequest.getUserData()).orElseThrow(InvalidRequestException::new);
            try {
                localAccountRepository.createLocalAccount(appuser, accountRequest.getId(), accountUtil.hash(accountRequest.getPassword(), salt), salt);
            } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                throw new InvalidException();
            }

            return issueTokenPair(appuser.getId());
        });
    }

//    public TokenPair SocialSignUp(CreateSocialUserRequest request) {
//        return tx.execute(() -> {
//
//        });
//    }

    public TokenPair login(AccountData accountData) {
        try {
            LocalAccount localAccount = localAccountRepository.findByLoginId(accountData.getLoginId())
                    .orElseThrow(AccountException::new);

            String salt = localAccount.getSalt();
            String savedPassword = localAccount.getPassword();

            if (accountUtil.matches(accountData.getPassword(), salt, savedPassword)) {
                String accessToken = jwtTokenProvider.createAccessToken(localAccount.getId());
                String refreshToken = jwtTokenProvider.createRefreshToken(localAccount.getId());

                redisUtil.save("refreshToken" + localAccount.getId(), refreshToken, 3600);

                return new TokenPair(accessToken, refreshToken);
            } else {
                throw new PasswordMisMatchException();
            }
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new InvalidException();
        }
    }

    public IdResponse changePassword(long userPk, ChangePasswordRequest request) {
        return tx.execute(() -> {
            try {
                AppUser appUser = userRepository.findById(userPk).orElseThrow(NotFoundException::new);
                LocalAccount localAccount = localAccountRepository.findByFk(appUser).orElseThrow(NotFoundException::new);

                String currentPassword = localAccount.getPassword();
                String hashedInputPassword = accountUtil.hash(request.getCurrentPassword(), localAccount.getSalt());

                if (currentPassword.equals(hashedInputPassword)) {
                    String newSalt = accountUtil.generateSalt();
                    String hashedPassword = accountUtil.hash(request.getNewPassword(), newSalt);

                    localAccountRepository.updatePassword(localAccount.getId(), hashedPassword, newSalt);

                    return new IdResponse(localAccount.getId());
                } else {
                    throw new PasswordMisMatchException();
                }

            } catch (NoSuchAlgorithmException |
                     InvalidKeySpecException e) {
                throw new InvalidException(e.getMessage());
            }
        });
    }

    public void logout(long userId) {
        redisUtil.delete(String.valueOf(userId));
    }

    public void deleteAccount(long userId, DeleteAccountRequest request) {
        tx.run(() -> {
            try {
                AppUser appUser = userRepository.findById(userId).orElseThrow(NotFoundException::new);
                LocalAccount localAccount = localAccountRepository.findByFk(appUser).orElseThrow(NotFoundException::new);

                String hashedInputPassword = accountUtil.hash(request.getCheckPassword(), localAccount.getSalt());

                if (hashedInputPassword.equals(localAccount.getPassword())) {
                    localAccountRepository.delete(localAccount.getId());
                    userRepository.delete(userId);

                } else {
                    throw new PasswordMisMatchException();
                }

            } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                throw new InvalidException(e.getMessage());
            }
        });

        redisUtil.delete("refresh" + userId + " : ");
    }

    private TokenPair issueTokenPair(long userId) {
        String accessToken = jwtTokenProvider.createAccessToken(userId);
        String refreshToken = jwtTokenProvider.createRefreshToken(userId);

        redisUtil.save("access" + userId +  " : ", jwtTokenProvider.getJti(accessToken) ,  15);
        redisUtil.save("refresh" + userId  +  " : ", jwtTokenProvider.getJti(refreshToken), 60);

        return new TokenPair(accessToken, refreshToken);
    }
}
