package controller.internal;

import com.minihttp.core.route.BaseRouter;
import com.minihttp.core.route.annotation.Delete;
import com.minihttp.core.route.annotation.Get;
import com.minihttp.core.route.annotation.Path;
import com.minihttp.core.route.annotation.Post;
import constant.Role;
import model.dto.response.DeleteResponse;
import model.dto.response.IdResponse;
import model.dto.response.UserRole.UserRoleResponse;
import service.internal.AdminUserRoleService;

/**
 * 관리자 전용 유저별 권한 라우터 (/internal/user/{user_id}/role)
 */
public class AdminUserRoleRouter extends BaseRouter {
    private final AdminUserRoleService adminUserRoleService;

    public AdminUserRoleRouter(AdminUserRoleService adminUserRoleService) {
        super("/internal/user");
        this.adminUserRoleService = adminUserRoleService;
    }

    /**
     * 유저 권한 조회 (GET /internal/user/{user_id}/role)
     * @param userId 조회할 유저 id
     * @return 유저가 가진 권한 목록
     */
    @Get("/{user_id}/role")
    public UserRoleResponse getRoleByUser(@Path("user_id") long userId) {
        return adminUserRoleService.getRoleByUser(userId);
    }

    /**
     * 유저 권한 부여 (POST /internal/user/{user_id}/role/{role})
     * @param userId 권한을 줄 유저 id
     * @param role   줄 권한 이름 (USER / MAKER / ADMIN)
     * @return 만들어진 유저-권한 연결 id
     */
    @Post("/{user_id}/role/{role}")
    public IdResponse assignRole(@Path("user_id") long userId, @Path("role") Role role) {
        return adminUserRoleService.assignRole(userId, role);
    }

    /**
     * 유저 권한 회수 (DELETE /internal/user/{user_id}/role/{role})
     * @param userId 권한을 뺄 유저 id
     * @param role   뺄 권한 이름 (USER / MAKER / ADMIN)
     * @return 삭제 여부
     */
    @Delete("/{user_id}/role/{role}")
    public DeleteResponse removeRole(@Path("user_id") long userId, @Path("role") Role role) {
        return adminUserRoleService.removeRole(userId, role);
    }
}
