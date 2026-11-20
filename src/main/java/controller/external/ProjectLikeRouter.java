package controller.external;

import auth.CurrentUser;
import com.minihttp.core.route.BaseRouter;
import com.minihttp.core.route.annotation.*;
import model.dto.request.like.LikeRequest;
import model.dto.response.DeleteResponse;
import model.dto.response.IdResponse;
import service.external.ProjectLikeService;

public class ProjectLikeRouter extends BaseRouter {
    private final ProjectLikeService projectLikeService;

    public ProjectLikeRouter(ProjectLikeService projectLikeService) {
        this.projectLikeService = projectLikeService;
        super("/project/like");
    }

    @Post("/{project_id}")
    public IdResponse createProjectLike(LikeRequest dto, @Path("project_id") Long projectId, @Depends(CurrentUser.class) Long userId) {
        return projectLikeService.createProjectLike(projectId, userId, dto);
    }

    @Patch("/{project_id}")
    public IdResponse updateProjectLike(LikeRequest dto, @Path("project_id") Long projectId, @Depends(CurrentUser.class) Long userId) {
        return projectLikeService.editProjectLike(projectId, userId, dto);
    }
    @Delete("/{project_id}")
    public DeleteResponse deleteProjectLike(@Path("project_id") Long projectId, @Depends(CurrentUser.class) Long userId) {
        return projectLikeService.deleteProjectLike(projectId, userId);
    }


}
