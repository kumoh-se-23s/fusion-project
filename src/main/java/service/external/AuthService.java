package service.external;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import config.AuthProperties;
import constant.SocialProvider;
import constant.RedisKey;
import exception.*;
import model.domain.AppUser;
import model.domain.LocalAccount;
import model.dto.AccountData;
import model.dto.SocialUserInfo;
import model.dto.request.account.DeleteAccountRequest;
import model.dto.response.TokenResponse;
import repository.LocalAccountRepository;
import repository.UserRepository;
import service.BaseService;
import service.OAuthClient;
import util.AccountUtil;
import util.JwtTokenProvider;
import util.RedisUtil;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.UUID;

public class AuthService extends BaseService {
    //소셜 로그인 콜백에서 만든 키는 10분 동안만 유효
    private static final int SOCIAL_KEY_EXPIRE_SECONDS = 600;

    private final JwtTokenProvider jwtTokenProvider;
    private final AccountUtil accountUtil;
    private final LocalAccountRepository localAccountRepository;
    private final UserRepository userRepository;
    private final OAuthClient oAuthClient;

    private final RedisUtil redisUtil;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public AuthService(JwtTokenProvider jwtTokenProvider, AccountUtil accountUtil, LocalAccountRepository localAccountRepository, UserRepository userRepository, RedisUtil redisUtil, OAuthClient oAuthClient) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.accountUtil = accountUtil;
        this.localAccountRepository = localAccountRepository;
        this.userRepository = userRepository;
        this.redisUtil = redisUtil;
        this.oAuthClient = oAuthClient;
    }

    /**
     * 로컬 로그인 (POST /auth/local-login)
     *
     * @param accountData 로그인 아이디, 비밀번호
     * @return access / refresh 토큰
     */
    public TokenResponse localLogin(AccountData accountData) {

        if (accountData.getPassword() == null) {
            throw new InvalidRequestException("비밀번호를 입력해야 합니다.");
        }

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
            } catch (NoSuchAlgorithmException e) {
                throw new InvalidException("알고리즘을 찾지 못함");
            } catch (InvalidKeySpecException e) {
                throw new InvalidException("설정 값이 잘못됨");
            }
        });
    }

    /**
     * 소셜 로그인 1단계: 소셜 제공자(카카오/구글 등) 로그인 화면 주소를 만든다
     */
    public String buildAuthorizationUrl(SocialProvider socialProvider) {
        return oAuthClient.getAuthorizationUrl(socialProvider);
    }

    /**
     * 소셜 로그인 2단계: 소셜 제공자가 돌려준 code로 유저 정보를 받아 온다 <br>
     * 이미 가입한 유저면 로그인 키를, 처음 온 유저면 회원가입 키를 Redis에 저장하고 프론트 주소를 돌려준다
     */
    public String handleCallback(SocialProvider socialProvider, String code, String state) throws JsonProcessingException {
        if (!oAuthClient.verifyState(state)) {
            throw new NotFoundException();
        }

        String socialAccessToken = oAuthClient.exchangeCode(socialProvider, code);
        SocialUserInfo socialUserInfo = oAuthClient.fetchUserInfo(socialProvider, socialAccessToken);

        String email = socialUserInfo.getEmail();

        String key = UUID.randomUUID().toString();

        if (userRepository.isExistEmail(email)) {
            AppUser appUser = userRepository.findByEmail(email).orElseThrow(NotFoundException::new);
            redisUtil.save(RedisKey.SOCIAL_LOGIN, key, appUser.getId(), SOCIAL_KEY_EXPIRE_SECONDS);

            return AuthProperties.get("front url") + "?code=" + key;
        }

        //회원가입 때 어떤 소셜 제공자로 가입하는지 알아야 하므로 제공자도 함께 저장한다
        SocialUserInfo signupInfo = SocialUserInfo.builder()
                .socialProvider(socialProvider)
                .providerUserId(socialUserInfo.getProviderUserId())
                .email(socialUserInfo.getEmail())
                .name(socialUserInfo.getName())
                .build();
        redisUtil.save(RedisKey.SOCIAL_SIGNUP, key, objectMapper.writeValueAsString(signupInfo), SOCIAL_KEY_EXPIRE_SECONDS);
        return AuthProperties.get("front signup url") + "?key=" + key;
    }

    //소셜 로그인 토큰 발급. 로컬 로그인과 같은 issueTokenPair를 써야 Redis 화이트리스트에 저장된다 <br>
    //userId만으로 토큰을 주므로 밖(라우터)에서 직접 부르지 못하게 private으로 막는다. exchangeLoginCode를 통해서만 쓴다
    private TokenResponse socialLogin(long userId) {
        return issueTokenPair(userId);
    }

    /**
     * 소셜 로그인 3단계: 프론트가 받은 로그인 키(code)를 토큰으로 바꿔 준다
     */
    public TokenResponse exchangeLoginCode(String code) {
        String userId = redisUtil.getValue(RedisKey.SOCIAL_LOGIN, code).orElseThrow(UnauthorizedException::new);
        redisUtil.delete(RedisKey.SOCIAL_LOGIN, code);
        return socialLogin(Long.parseLong(userId));
    }

    public void logout(long userId) {
        redisUtil.delete(RedisKey.ACCESS, userId);
        redisUtil.delete(RedisKey.REFRESH, userId);
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

            } catch (NoSuchAlgorithmException e) {
                throw new InvalidException("알고리즘을 못 찾음");
            } catch (InvalidKeySpecException e) {
                throw new InvalidException("설정 값이 잘못됨");
            }
        });

        redisUtil.delete(RedisKey.REFRESH, userId);
    }

    //로그인/회원가입 후 토큰 발급 (UserService.localSignUp에서도 사용)
    public TokenResponse issueTokenPair(long userId) {
        String accessToken = jwtTokenProvider.createAccessToken(userId);
        String refreshToken = jwtTokenProvider.createRefreshToken(userId);

        //Redis 저장 시간 = JWT 만료 시간 (setex는 초 단위)
        redisUtil.save(RedisKey.ACCESS, userId, jwtTokenProvider.getJti(accessToken), JwtTokenProvider.ACCESS_TOKEN_EXPIRE_SECONDS);
        redisUtil.save(RedisKey.REFRESH, userId, jwtTokenProvider.getJti(refreshToken), JwtTokenProvider.REFRESH_TOKEN_EXPIRE_SECONDS);

        return new TokenResponse(accessToken, refreshToken);
    }
}
