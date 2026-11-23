package controller.internal;

import com.minihttp.core.route.BaseRouter;
import com.minihttp.core.route.annotation.Delete;
import com.minihttp.core.route.annotation.Get;
import com.minihttp.core.route.annotation.Path;
import model.dto.response.DeleteResponse;
import model.dto.response.account.LocalAccountResponse;
import model.dto.response.account.SocialAccountResponse;
import service.internal.AdminLocalAccountService;
import service.internal.AdminSocialAccountService;

/**
 * 관리자 전용 계정 관리 라우터 (/internal/account)
 */
public class AdminAccountRouter extends BaseRouter {
    private final AdminSocialAccountService adminSocialAccountService;
    private final AdminLocalAccountService adminLocalAccountService;

    public AdminAccountRouter(AdminSocialAccountService adminSocialAccountService,
                              AdminLocalAccountService adminLocalAccountService) {
        super("/internal/account");
        this.adminSocialAccountService = adminSocialAccountService;
        this.adminLocalAccountService = adminLocalAccountService;
    }

    /**
     * 소셜 계정 단건 조회 (GET /internal/account/social/{social_account_id})
     * @param socialAccountId 조회할 소셜 계정 id
     * @return 소셜 계정 정보
     */
    @Get("/social/{social_account_id}")
    public SocialAccountResponse getSocialAccount(@Path("social_account_id") long socialAccountId) {
        return adminSocialAccountService.getSocialAccount(socialAccountId);
    }

    /**
     * 소셜 계정 삭제 (DELETE /internal/account/social/{social_account_id})
     * @param socialAccountId 삭제할 소셜 계정 id
     * @return 삭제 여부
     */
    @Delete("/social/{social_account_id}")
    public DeleteResponse deleteSocialAccount(@Path("social_account_id") long socialAccountId) {
        return adminSocialAccountService.deleteSocialAccount(socialAccountId);
    }

    /**
     * 로컬 계정 단건 조회 (GET /internal/account/local/{local_account_id})
     * @param localAccountId 조회할 로컬 계정 id
     * @return 로컬 계정 정보
     */
    @Get("/local/{local_account_id}")
    public LocalAccountResponse getLocalAccount(@Path("local_account_id") long localAccountId) {
        return adminLocalAccountService.getLocalAccount(localAccountId);
    }
}
