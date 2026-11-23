package controller.external;

import auth.CurrentUser;
import com.minihttp.core.route.BaseRouter;
import com.minihttp.core.route.annotation.Depends;
import com.minihttp.core.route.annotation.Get;
import com.minihttp.core.route.annotation.Post;
import com.minihttp.core.route.annotation.Query;
import model.dto.AccountData;
import model.dto.response.TokenResponse;
import model.dto.response.account.AvailableResponse;
import service.external.AuthService;
import service.external.LocalAccountService;

/**
 * 인증 관련 API 라우터 (/auth)
 *
 * <p>TODO 남은 문제점
 * <ol>
 *   <li>[미구현] 소셜 로그인 1단계(로그인 화면 주소, buildAuthorizationUrl), 2단계(콜백, handleCallback) 엔드포인트</li>
 *   <li>[미구현] 토큰 재발급 엔드포인트 (AuthService에 메서드도 아직 없음)</li>
 *   <li>[미구현] 소셜 회원가입 엔드포인트 (SocialAuthService.socialSignUp 사용)</li>
 *   <li>[확인] 노션 명세의 소셜 로그인 경로는 /social-login/{provider} 이다. 코드와 노션 중 하나로 맞춘다.</li>
 * </ol>
 */
public class AuthRouter extends BaseRouter {
    private final AuthService authService;
    private final LocalAccountService localAccountService;

    public AuthRouter(AuthService authService, LocalAccountService localAccountService) {
        super("/auth");
        this.authService = authService;
        this.localAccountService = localAccountService;
    }

    @Post("/local-login")
    public TokenResponse localLogin(AccountData accountData) {
        return authService.localLogin(accountData);
    }

    /**
     * 소셜 로그인 (POST /auth/social-login?code={code}) <br>
     * 소셜 로그인 콜백이 프론트로 넘겨준 일회용 code를 토큰으로 바꿔 준다 <br>
     * code는 카카오/구글 확인을 통과한 사람만 받을 수 있고, 10분 뒤나 한 번 쓰면 사라진다
     * @param code 콜백에서 받은 일회용 로그인 키
     * @return access / refresh 토큰
     */
    @Post("/social-login")
    public TokenResponse socialLogin(@Query("code") String code) {
        return authService.exchangeLoginCode(code);
    }

    /**
     * 로그아웃 (POST /auth/logout) <br>
     * 로그인한 본인의 토큰만 지운다
     * @param userId 토큰에서 꺼낸 로그인한 유저 id
     */
    @Post("/logout")
    public void logout(@Depends(CurrentUser.class) Long userId) {
        authService.logout(userId);
    }

    @Get("/check-login-id")
    public AvailableResponse checkDuplicateLoginId(@Query("login_id") String loginId) {
        return localAccountService.checkDuplicateLoginId(loginId);
    }
}
