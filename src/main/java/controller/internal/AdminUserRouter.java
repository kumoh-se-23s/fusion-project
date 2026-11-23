package controller.internal;

import com.minihttp.core.route.BaseRouter;
import com.minihttp.core.route.annotation.Delete;
import com.minihttp.core.route.annotation.Get;
import com.minihttp.core.route.annotation.Patch;
import com.minihttp.core.route.annotation.Path;
import com.minihttp.core.route.annotation.Query;
import model.dto.request.user.AdminUpdateUserRequest;
import model.dto.response.DeleteResponse;
import model.dto.response.IdResponse;
import model.dto.response.user.AdminUserListResponse;
import model.dto.response.user.AdminUserResponse;
import service.internal.AdminUserService;

/**
 * 관리자 전용 유저 CRUD 라우터 (/internal/user)
 *
 * <p>TODO
 * <ul>
 *   <li>[미구현] 유저 생성 (POST /internal/user): AdminUserService에 생성 메서드가 아직 없다.</li>
 * </ul>
 */
public class AdminUserRouter extends BaseRouter {
    private final AdminUserService adminUserService;

    public AdminUserRouter(AdminUserService adminUserService) {
        super("/internal/user");
        this.adminUserService = adminUserService;
    }

    /**
     * 유저 전체 조회 (GET /internal/user?limit={amount})
     * @param limit 조회 상한 (보내지 않으면 전체)
     * @return 유저 목록
     */
    @Get()
    public AdminUserListResponse getAllUsers(@Query(value = "limit", required = false) Integer limit) {
        return adminUserService.getAllUsers(limit);
    }

    /**
     * 유저 단건 조회 (GET /internal/user/{user_id})
     * @param userId 조회할 유저 id
     * @return 유저 정보
     */
    @Get("/{user_id}")
    public AdminUserResponse getUser(@Path("user_id") long userId) {
        return adminUserService.getUser(userId);
    }

    /**
     * 유저 수정 (PATCH /internal/user/{user_id})
     * @param userId  수정할 유저 id
     * @param request 수정할 값
     * @return 수정한 유저 id
     */
    @Patch("/{user_id}")
    public IdResponse editUser(@Path("user_id") long userId, AdminUpdateUserRequest request) {
        return adminUserService.editUser(userId, request);
    }

    /**
     * 유저 삭제 (DELETE /internal/user/{user_id})
     * @param userId 삭제할 유저 id
     * @return 삭제 여부
     */
    @Delete("/{user_id}")
    public DeleteResponse deleteUser(@Path("user_id") long userId) {
        return adminUserService.deleteUser(userId);
    }
}
