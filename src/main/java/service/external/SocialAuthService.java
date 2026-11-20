package service.external;

import constant.SocialProvider;
import exception.InvalidException;
import exception.NotFoundException;
import model.domain.AppUser;
import model.dto.SocialUserInfo;
import model.dto.TokenPair;
import model.dto.request.user.CreateSocialUserRequest;
import repository.UserRepository;
import service.BaseService;
import service.OAuthClient;
import util.JwtTokenProvider;
import util.RedisUtil;

import java.util.List;

public class SocialAuthService extends BaseService {
    private final RedisUtil redisUtil;
    private final OAuthClient oAuthClient;
    private final JwtTokenProvider jwtTokenProvider;

    private final UserRepository userRepository;

    public SocialAuthService(RedisUtil redisUtil, OAuthClient oAuthClient, JwtTokenProvider jwtTokenProvider, UserRepository userRepository) {
        this.redisUtil = redisUtil;
        this.oAuthClient = oAuthClient;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
    }

    public String buildAuthorizationUrl(SocialProvider socialProvider, String state) {
        return oAuthClient.getAuthorizationUrl(socialProvider);
    }

    public TokenPair handleCallback(SocialProvider socialProvider, String code, String state) {
        if (!oAuthClient.verifyState(state)) {
            throw new NotFoundException();
        }

        String exchangedCdoe = oAuthClient.exchangeCode(socialProvider, code);

        SocialUserInfo socialUserInfo = oAuthClient.fetchUserInfo(socialProvider,exchangedCdoe);

        String email = socialUserInfo.getEmail();

        //리다이렉트 주소 나눌 예정
        if (userRepository.isExistEmail(email)) {
            AppUser appUser = userRepository.findByEmail(email).orElseThrow(InvalidException::new);
            return socialLogin(appUser.getId());
        } else {
            socialSignUp();
        }
    }


    public TokenPair socialLogin(long userId) {
        String accessToken = jwtTokenProvider.createAccessToken(userId);
        String refreshToken = jwtTokenProvider.createRefreshToken(userId);

        return new TokenPair(accessToken, refreshToken);
    }

    public TokenPair socialSignUp(CreateSocialUserRequest request, String email, String name) {
        return tx.execute(() -> {
            redisUtil.getValue()
            AppUser appUser = userRepository.createUser()

        });
    }
}
