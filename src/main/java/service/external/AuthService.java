package service.external;

import com.sun.jdi.request.InvalidRequestStateException;
import exception.*;
import model.domain.AppUser;
import model.domain.LocalAccount;
import model.dto.AccountData;
import model.dto.TokenPair;
import model.dto.request.account.CreateLocalAccountRequest;
import model.dto.request.account.DeleteAccountRequest;
import model.dto.request.user.CreateSocialUserRequest;
import model.dto.request.user.CreateUserRequest;
import model.dto.response.IdResponse;
import model.dto.response.TokenResponse;
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

    /**
     * 로컬 회원가입
     * @param accountRequest
     * @param userRequest
     * @return TokenResponse
     */
    public TokenResponse localSignUp(CreateLocalAccountRequest accountRequest, CreateUserRequest userRequest) {
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

    /**
     * 로컬 로그인 (POST /account/local-login)
     *
     * @param accountData 로그인 아이디, 비밀번호
     * @return access / refresh 토큰
     */
    public TokenResponse localLogin(AccountData accountData) {
        //계정 조회만 하므로 읽기 전용 트랜잭션
        return tx.execute(true, () -> {
            try {
                LocalAccount localAccount = localAccountRepository.findByLoginId(accountData.getLoginId())
                        .orElseThrow(AccountException::new);

                String salt = localAccount.getSalt();
                String savedPassword = localAccount.getPassword();

                if (!accountUtil.matches(accountData.getPassword(), salt, savedPassword)) {
                    throw new PasswordMisMatchException();
                }

                //토큰에는 로컬 계정 id가 아니라 유저 id를 넣는다 (다른 API가 모두 유저 id로 사용자를 찾음)
                //Redis 저장도 회원가입과 같은 방식(키: access/refresh + userId, 값: jti)으로 맞춘다
                return issueTokenPair(localAccount.getUser().getId());
            } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                throw new InvalidException();
            }
        });
    }

    public void logout(long userId) {
        redisUtil.delete("access" + userId + " : ");
        redisUtil.delete("refresh" + userId + " : ");
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

    private TokenResponse issueTokenPair(long userId) {
        String accessToken = jwtTokenProvider.createAccessToken(userId);
        String refreshToken = jwtTokenProvider.createRefreshToken(userId);

        //Redis 저장 시간 = JWT 만료 시간 (setex는 초 단위)
        redisUtil.save("access" + userId + " : ", jwtTokenProvider.getJti(accessToken), JwtTokenProvider.ACCESS_TOKEN_EXPIRE_SECONDS);
        redisUtil.save("refresh" + userId + " : ", jwtTokenProvider.getJti(refreshToken), JwtTokenProvider.REFRESH_TOKEN_EXPIRE_SECONDS);

        return new TokenResponse(accessToken, refreshToken);
    }
}
