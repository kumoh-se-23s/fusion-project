package service.external;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import constant.RedisKey;
import constant.Role;
import exception.*;
import model.domain.AppUser;
import model.domain.UserRole;
import model.domain.UserRoleRelation;
import model.dto.SocialUserInfo;
import model.dto.request.user.CreateSocialUserRequest;
import model.dto.response.TokenResponse;
import repository.SocialAccountRepository;
import repository.UserRepository;
import repository.UserRoleRelationRepository;
import repository.UserRoleRepository;
import service.BaseService;
import util.RedisUtil;

public class SocialAuthService extends BaseService {
    private static final int BAD_REQUEST = 400;

    private final RedisUtil redisUtil;
    private final UserRepository userRepository;
    private final SocialAccountRepository socialAccountRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserRoleRelationRepository userRoleRelationRepository;
    private final AuthService authService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public SocialAuthService(RedisUtil redisUtil,
                             UserRepository userRepository, SocialAccountRepository socialAccountRepository,
                             UserRoleRepository userRoleRepository, UserRoleRelationRepository userRoleRelationRepository,
                             AuthService authService) {
        this.redisUtil = redisUtil;
        this.userRepository = userRepository;
        this.socialAccountRepository = socialAccountRepository;
        this.userRoleRepository = userRoleRepository;
        this.userRoleRelationRepository = userRoleRelationRepository;
        this.authService = authService;
    }

    //소셜 로그인(buildAuthorizationUrl, handleCallback, socialLogin, exchangeLoginCode)은 AuthService로 옮김

    /**
     * 소셜 회원가입 (POST /account/social-signup?key={key}) <br>
     * 소셜 로그인 콜백에서 처음 온 유저면 key를 받아 회원가입 화면으로 간다. 그 화면에서 추가 정보를 받아 가입시킨다 <br>
     * 유저(APP_USERS)와 소셜 계정(SOCIAL_ACCOUNT)을 함께 만들고 기본 권한(USER)을 준 뒤, 바로 로그인된 토큰을 돌려준다
     *
     * @param key     콜백에서 받은 회원가입 키 (10분 동안만 유효, 없거나 만료면 401)
     * @param request 소셜 계정으로 받을 수 없는 추가 정보 (닉네임 필수)
     * @return TokenResponse access / refresh 토큰
     */
    public TokenResponse socialSignUp(String key, CreateSocialUserRequest request) {
        if (request.getNickname() == null || request.getNickname().isBlank()) {
            throw new InvalidRequestException("닉네임은 꼭 입력해야 합니다");
        }

        //가입이 실패해도(예: 닉네임 중복) 다시 시도할 수 있도록, 키는 가입이 끝난 뒤에 지운다
        //회원가입 키는 AuthService.handleCallback에서 저장한다
        SocialUserInfo info = readSignupInfo(key);

        long userId = tx.execute(() -> {
            if (socialAccountRepository.isExistSocialIdentity(info.getSocialProvider(), info.getProviderUserId())) {
                throw new AlreadyExistException("이미 가입된 소셜 계정입니다");
            }
            if (userRepository.isExistEmail(info.getEmail())) {
                throw new AlreadyExistException("이미 사용 중인 이메일입니다");
            }
            if (userRepository.isExistNickname(request.getNickname())) {
                throw new AlreadyExistException("이미 사용 중인 닉네임입니다");
            }

            //이름, 이메일은 소셜 제공자가 준 값, 나머지는 회원가입 화면에서 받은 값
            AppUser user = userRepository.create(AppUser.builder()
                    .name(info.getName())
                    .email(info.getEmail())
                    .nickname(request.getNickname())
                    .postal(request.getPostalCode())
                    .address(request.getAddress())
                    .phone(request.getPhone())
                    .build());

            socialAccountRepository.createSocialAccount(info.getProviderUserId(), user, info.getSocialProvider());

            //기본 권한(USER) 부여. USER 권한 데이터가 없으면 서버 설정 문제이므로 롤백
            UserRole userRole = userRoleRepository.findByRole(Role.USER)
                    .orElseThrow(() -> new NotFoundException("기본 권한(USER)이 없습니다"));
            userRoleRelationRepository.create(UserRoleRelation.builder()
                    .user(user)
                    .role(userRole)
                    .build());

            return user.getId();
        });

        redisUtil.delete(RedisKey.SOCIAL_SIGNUP, key);

        //가입이 커밋된 뒤에 토큰을 만든다 (로컬 회원가입과 같은 방식, Redis 화이트리스트에도 저장됨)
        return authService.issueTokenPair(userId);
    }

    //회원가입 키로 저장해 둔 소셜 정보를 읽는다. 없거나 만료면 401
    private SocialUserInfo readSignupInfo(Object object) {
        String json = redisUtil.getValue(RedisKey.SOCIAL_SIGNUP,object).orElseThrow(UnauthorizedException::new);
        try {
            return objectMapper.readValue(json, SocialUserInfo.class);
        } catch (JsonProcessingException e) {
            throw new InvalidException("Json을 처리하지 못함");
        }
    }
}
