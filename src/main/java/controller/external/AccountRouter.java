package controller.external;

import auth.CurrentUser;
import com.minihttp.core.route.BaseRouter;
import com.minihttp.core.route.annotation.Delete;
import com.minihttp.core.route.annotation.Depends;
import com.minihttp.core.route.annotation.Get;
import com.minihttp.core.route.annotation.Patch;
import com.minihttp.core.route.annotation.Path;
import com.minihttp.core.route.annotation.Post;
import constant.SocialProvider;
import model.dto.request.account.ChangePasswordRequest;
import model.dto.response.DeleteResponse;
import model.dto.response.IdResponse;
import model.dto.response.MyRoleResponse;
import model.dto.response.account.MySocialAccountResponse;
import model.dto.response.account.SocialAccountResponse;
import service.external.LocalAccountService;
import service.external.SocialAccountService;
import service.external.UserRoleService;
import service.external.UserService;

public class AccountRouter extends BaseRouter {
    private final UserService userService;
    private final LocalAccountService localAccountService;
    private final SocialAccountService socialAccountService;
    private final UserRoleService userRoleService;

    public AccountRouter(UserService userService, LocalAccountService localAccountService,
                         SocialAccountService socialAccountService, UserRoleService userRoleService) {
        super("/account");
        this.userService = userService;
        this.localAccountService = localAccountService;
        this.socialAccountService = socialAccountService;
        this.userRoleService = userRoleService;
    }

    @Delete("/{local_account_id}")
    public DeleteResponse deleteLocalAccount(@Depends(CurrentUser.class) Long userId,
                                             @Path("local_account_id") long localAccountId) {
        return userService.deleteLocalAccount(userId, localAccountId);
    }

    @Patch("/change-password")
    public IdResponse changePassword(@Depends(CurrentUser.class) Long userId, ChangePasswordRequest request) {
        return localAccountService.changePassword(userId, request);
    }

    @Get("/social")
    public MySocialAccountResponse getMySocialAccount(@Depends(CurrentUser.class) Long userId) {
        return socialAccountService.getMySocialAccount(userId);
    }

    @Post("/social/{provider}")
    public SocialAccountResponse linkSocialAccount(@Depends(CurrentUser.class) Long userId, String code, SocialProvider provider) {
        return socialAccountService.linkSocialAccount(userId, code, provider);
    }

    @Delete("/social/{provider}/{social_account_id}")
    public DeleteResponse unlinkedSocialAccount(@Depends(CurrentUser.class) Long userId,
                                                @Path("social_account_id") long socialAccountId,
                                                SocialProvider provider) {
        return socialAccountService.unlinkedSocialAccount(userId, socialAccountId, provider);
    }

    /**
     * 내 권한 조회 (GET /account/role) <br>
     * 로그인한 유저가 가진 권한 목록(USER, MAKER, ADMIN ...)을 돌려준다
     */
    @Get("/role")
    public MyRoleResponse getMyRole(@Depends(CurrentUser.class) Long userId) {
        return userRoleService.getCurrentUserPermissions(userId);
    }
}
