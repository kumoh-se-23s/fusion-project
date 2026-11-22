package controller.external;

import com.minihttp.core.route.BaseRouter;
import com.minihttp.core.route.annotation.Get;
import com.minihttp.core.route.annotation.Post;
import model.dto.TokenPair;
import model.dto.response.account.LoginResponse;
import service.external.AuthService;

public class AuthRouter extends BaseRouter {
    private final AuthService authService ;

    public AuthRouter(AuthService authService) {
        super("/auth");
        this.authService = authService;
    }

    //로컬 회원가입은 UserService.localSignUp (POST /user/local-register)으로 옮김

    @Post()
    //public socialSignup

    @Post()
    public LoginResponse login() {

    }


}
