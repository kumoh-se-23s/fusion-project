package service.external;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import config.AuthProperties;
import constant.SocialProvider;
import exception.InvalidException;
import exception.NotFoundException;
import exception.UnauthorizedException;
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
import java.util.UUID;

public class SocialAuthService extends BaseService {
    private final RedisUtil redisUtil;
    private final OAuthClient oAuthClient;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public SocialAuthService(RedisUtil redisUtil, OAuthClient oAuthClient, JwtTokenProvider jwtTokenProvider, UserRepository userRepository) {
        this.redisUtil = redisUtil;
        this.oAuthClient = oAuthClient;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
    }

    public String buildAuthorizationUrl(SocialProvider socialProvider, String state) {
        return oAuthClient.getAuthorizationUrl(socialProvider);
    }

    public String handleCallback(SocialProvider socialProvider, String code, String state) throws JsonProcessingException {
        if (!oAuthClient.verifyState(state)) {
            throw new NotFoundException();
        }

        String socialAccessToken = oAuthClient.exchangeCode(socialProvider, code);
        SocialUserInfo socialUserInfo = oAuthClient.fetchUserInfo(socialProvider, socialAccessToken);

        String email = socialUserInfo.getEmail();

        String key = UUID.randomUUID().toString();

        if (userRepository.isExistEmail(email)) {
            AppUser appUser = userRepository.findByEmail(email).orElseThrow(InvalidException::new);
            redisUtil.save("social-login:" + key, appUser.getId(), 600);

            return AuthProperties.get("front url") + "?code=" + key;
        }

        redisUtil.save("social-signup:"+ key, objectMapper.writeValueAsString(socialUserInfo), 600);
        return AuthProperties.get("front signup url") + "?key=" + key;
    }


    public TokenPair socialLogin(long userId) {
        String accessToken = jwtTokenProvider.createAccessToken(userId);
        String refreshToken = jwtTokenProvider.createRefreshToken(userId);

        return new TokenPair(accessToken, refreshToken);
    }
//
//    public TokenPair socialSignUp(CreateSocialUserRequest request, String email, String name) {
//        return tx.execute(() -> {
//
//        });
//    }

    public TokenPair exchangeLoginCode(String code) {
        String redisKey = "social login url:" + code;
        String userId = redisUtil.getValue(redisKey).orElseThrow(UnauthorizedException::new);
        redisUtil.delete(redisKey);
        return socialLogin(Long.parseLong(userId));
    }

    public SocialUserInfo takeSignupInfo(String key) throws JsonProcessingException {
        String redisKey = "social-signup url:" + key;
        String json = redisUtil.getValue(redisKey).orElseThrow(UnauthorizedException::new);
        redisUtil.delete(redisKey);
        return objectMapper.readValue(json, SocialUserInfo.class);
    }
}
