package service.external;

import constant.Role;
import exception.AlreadyExistException;
import exception.InvalidException;
import exception.InvalidRequestException;
import exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import model.domain.AppUser;
import model.domain.LocalAccount;
import model.domain.UserRole;
import model.domain.UserRoleRelation;
import model.dto.request.user.LocalSignUpRequest;
import model.dto.request.user.UpdateUserRequest;
import model.dto.response.DeleteResponse;
import model.dto.response.IdResponse;
import model.dto.response.TokenResponse;
import model.dto.response.account.AvailableResponse;
import model.dto.response.user.MyInfoResponse;
import model.dto.response.user.UserPublicResponse;
import repository.LocalAccountRepository;
import repository.UserRepository;
import repository.UserRoleRelationRepository;
import repository.UserRoleRepository;
import service.BaseService;
import util.AccountUtil;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

@RequiredArgsConstructor
public class UserService extends BaseService {
    private static final int BAD_REQUEST = 400;

    private final LocalAccountRepository localAccountRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserRoleRelationRepository userRoleRelationRepository;
    private final AccountUtil accountUtil;
    private final AuthService authService;

    /**
     * 로컬 유저 회원가입 (POST /user/local-register) <br>
     * 유저(APP_USERS)와 로컬 계정(LOCAL_ACCOUNT)을 함께 만들고 기본 권한(USER)을 준 뒤, 바로 로그인된 토큰을 돌려준다 <br>
     * 아이디 / 닉네임 / 이메일이 이미 있으면 409
     *
     * @param request 계정 정보 + 유저 정보
     * @return TokenResponse access / refresh 토큰
     */
    public TokenResponse localSignUp(LocalSignUpRequest request) {
        validateSignUp(request);

        long userId = tx.execute(() -> {
            if (localAccountRepository.isExistLoginId(request.getLoginId())) {
                throw new AlreadyExistException("이미 사용 중인 아이디입니다");
            }
            if (userRepository.isExistNickname(request.getNickname())) {
                throw new AlreadyExistException("이미 사용 중인 닉네임입니다");
            }
            if (userRepository.isExistEmail(request.getEmail())) {
                throw new AlreadyExistException("이미 사용 중인 이메일입니다");
            }

            AppUser user = userRepository.create(AppUser.builder()
                    .name(request.getName())
                    .nickname(request.getNickname())
                    .email(request.getEmail())
                    .postal(request.getPostalCode())
                    .address(request.getAddress())
                    .phone(request.getPhone())
                    .build());

            String salt = accountUtil.generateSalt();
            try {
                localAccountRepository.createLocalAccount(user, request.getLoginId(), accountUtil.hash(request.getPassword(), salt), salt);
            } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                throw new InvalidException(e.getMessage());
            }

            //기본 권한(USER) 부여. USER 권한 데이터가 없으면 서버 설정 문제이므로 롤백
            UserRole userRole = userRoleRepository.findByRole(Role.USER)
                    .orElseThrow(() -> new NotFoundException("기본 권한(USER)이 없습니다"));
            userRoleRelationRepository.create(UserRoleRelation.builder()
                    .user(user)
                    .role(userRole)
                    .build());

            return user.getId();
        });

        //가입이 커밋된 뒤에 토큰을 만든다 (가입이 실패하면 Redis에 토큰이 남지 않도록)
        return authService.issueTokenPair(userId);
    }

    //필수 값이 비어 있으면 400
    private void validateSignUp(LocalSignUpRequest request) {
        if (isBlank(request.getLoginId()) || isBlank(request.getPassword())
                || isBlank(request.getName()) || isBlank(request.getNickname()) || isBlank(request.getEmail())) {
            throw new InvalidRequestException("아이디, 비밀번호, 이름, 닉네임, 이메일은 꼭 입력해야 합니다", BAD_REQUEST);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    /**
     * 내 정보 조회 (GET /user/me) <br>
     * 로그인한 유저 본인의 정보를 명세서의 응답 형태로 돌려준다
     *
     * @param userId 토큰에서 꺼낸 로그인한 유저 id (믿을 수 있는 값)
     * @return MyInfoResponse
     */
    public MyInfoResponse getUser(long userId) {
        return tx.execute(true, () -> {
            AppUser user = userRepository.findById(userId).orElseThrow(NotFoundException::new);
            return MyInfoResponse.builder()
                    .userId(user.getId())
                    .name(user.getName())
                    .nickname(user.getNickname())
                    .email(user.getEmail())
                    .postalCode(user.getPostal())
                    .address(user.getAddress())
                    .phone(user.getPhone())
                    .createdAt(user.getCreatedAt())
                    .build();
        });
    }

    /**
     * 유저 정보 조회 (GET /user/{user_id}) <br>
     * 다른 유저의 공개 정보(id, 닉네임)만 돌려준다. 없으면 404 <br>
     * 게스트도 부를 수 있으므로 이메일, 전화번호, 주소 같은 개인정보는 내보내지 않는다
     *
     * @param userId 조회할 유저 id (주소로 들어온 값)
     * @return UserPublicResponse
     */
    public UserPublicResponse getUserById(long userId) {
        return tx.execute(true, () -> {
            AppUser user = userRepository.findById(userId).orElseThrow(NotFoundException::new);
            return UserPublicResponse.builder()
                    .userId(user.getId())
                    .nickname(user.getNickname())
                    .build();
        });
    }

    /**
     * 내 정보 수정 (PATCH /user/me) <br>
     * 보낸 값만 수정한다. 다른 유저가 쓰는 닉네임으로 바꾸려 하면 409
     *
     * @param userId  토큰에서 꺼낸 로그인한 유저 id (믿을 수 있는 값)
     * @param request 수정할 값 (보내지 않은 값은 null)
     * @return IdResponse 수정된 유저 id
     */
    public IdResponse updateMyInfo(long userId, UpdateUserRequest request) {
        return tx.execute(() -> {
            AppUser user = userRepository.findById(userId).orElseThrow(NotFoundException::new);

            //닉네임을 보냈고, 지금 내 닉네임과 다를 때만 중복 검사 (안 보냈으면 null)
            String nickname = request.getNickname();
            if (nickname != null
                    && !nickname.equals(user.getNickname())
                    && userRepository.isExistNickname(nickname)) {
                throw new AlreadyExistException("이미 사용 중인 닉네임입니다");
            }

            Long updateResult = userRepository.updateUser(userId, request.toUpdateMap()).orElseThrow(NotFoundException::new);

            return new IdResponse(updateResult);
        });
    }

    /**
     * 계정 삭제 (회원 탈퇴) <br>
     * 로그인한 유저 본인만 탈퇴할 수 있다. 유저를 지우면 로컬/소셜 계정은 CASCADE로 함께 지워진다
     *
     * @param userId         토큰에서 꺼낸 로그인한 유저 id (믿을 수 있는 값)
     * @param localAccountId 요청으로 들어온 로컬 계정 id (조작될 수 있는 값)
     * @return DeleteResponse
     */
    public DeleteResponse deleteLocalAccount(long userId, long localAccountId) {
        return tx.execute(() -> {
            LocalAccount localAccount = localAccountRepository.findById(localAccountId).orElseThrow(NotFoundException::new);

            //남의 계정이면 존재 여부를 알려 주지 않도록 403 대신 404
            if (!localAccount.getUser().getId().equals(userId)) {
                throw new NotFoundException();
            }

            boolean result = userRepository.delete(userId);
            return new DeleteResponse(result);
        });
    }


    public AvailableResponse isEmailAvailable(String email) {
        return tx.execute(true, () -> {
            boolean result = userRepository.isExistEmail(email);
            return new AvailableResponse(!result);
        });
    }

    public AvailableResponse isNicknameAvailable(String nickname) {
        return tx.execute(true, () -> {
            boolean result = userRepository.isExistNickname(nickname);
            return new AvailableResponse(!result);
        });
    }
}
