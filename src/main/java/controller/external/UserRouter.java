package controller.external;

import auth.CurrentUser;
import com.minihttp.core.route.BaseRouter;
import com.minihttp.core.route.annotation.Depends;
import com.minihttp.core.route.annotation.Get;
import com.minihttp.core.route.annotation.Patch;
import com.minihttp.core.route.annotation.Path;
import com.minihttp.core.route.annotation.Post;
import com.minihttp.core.route.annotation.Query;
import model.dto.request.user.LocalSignUpRequest;
import model.dto.request.user.UpdateUserRequest;
import model.dto.response.IdResponse;
import model.dto.response.TokenResponse;
import model.dto.response.account.AvailableResponse;
import model.dto.response.user.MyInfoResponse;
import model.dto.response.user.UserPublicResponse;
import service.external.UserService;

/**
 * 회원 정보(프로필) API 라우터 (/user)
 */
public class UserRouter extends BaseRouter {
    private final UserService userService;

    public UserRouter(UserService userService) {
        super("/user");
        this.userService = userService;
    }

    /**
     * 로컬 유저 회원가입 (POST /user/local-register)
     * @param request 계정 정보 + 유저 정보
     * @return access / refresh 토큰 (가입 후 바로 로그인)
     */
    @Post("/local-register")
    public TokenResponse localSignUp(LocalSignUpRequest request) {
        return userService.localSignUp(request);
    }

    /**
     * 이메일 중복 확인 (GET /user/check-email?email={email})
     * @param email 확인할 이메일
     * @return 사용 가능 여부
     */
    @Get("/check-email")
    public AvailableResponse checkEmail(@Query("email") String email) {
        return userService.isEmailAvailable(email);
    }

    /**
     * 닉네임 중복 확인 (GET /user/check-nickname?nickname={nickname})
     * @param nickname 확인할 닉네임
     * @return 사용 가능 여부
     */
    @Get("/check-nickname")
    public AvailableResponse checkNickname(@Query("nickname") String nickname) {
        return userService.isNicknameAvailable(nickname);
    }

    /**
     * 내 정보 조회 (GET /user/me)
     * @param userId 토큰에서 꺼낸 로그인한 유저 id
     * @return 내 정보
     */
    @Get("/me")
    public MyInfoResponse getMyInfo(@Depends(CurrentUser.class) Long userId) {
        return userService.getUser(userId);
    }

    /**
     * 내 정보 수정 (PATCH /user/me)
     * @param userId  토큰에서 꺼낸 로그인한 유저 id
     * @param request 수정할 값 (보내지 않은 값은 null)
     * @return 수정한 유저 id
     */
    @Patch("/me")
    public IdResponse updateMyInfo(@Depends(CurrentUser.class) Long userId, UpdateUserRequest request) {
        return userService.updateMyInfo(userId, request);
    }

    /**
     * 유저 정보 조회 (GET /user/{user_id}) <br>
     * 고정 경로(/user/me 등)가 변수 경로보다 먼저 맞춰지므로 /user/me와 겹치지 않는다
     * @param userId 조회할 유저 id
     * @return 다른 사람에게 보여도 되는 공개 정보
     */
    @Get("/{user_id}")
    public UserPublicResponse getUserById(@Path("user_id") long userId) {
        return userService.getUserById(userId);
    }
}
