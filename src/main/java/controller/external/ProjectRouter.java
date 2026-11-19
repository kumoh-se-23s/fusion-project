package controller.external;

import auth.CurrentUser;
import com.minihttp.core.route.BaseRouter;
import com.minihttp.core.route.annotation.*;
import model.dto.ProjectData;
import model.dto.request.project.CreateProjectRequest;
import model.dto.request.project.EditProjectRequest;
import model.dto.response.DataCollectionResponse;
import model.dto.response.DeleteResponse;
import model.dto.response.IdResponse;
import model.dto.response.SingleDataResponse;
import service.external.ProjectMakerService;
import service.external.ProjectQueryService;

import java.util.List;
import java.util.Optional;

/**
 * 관리자 전용 프로젝트 라우터
 * @author 김민규
 * @since 2026-11-16
 * @version 1.0
 */
public class ProjectRouter extends BaseRouter {
    private final ProjectMakerService projectMakerService;
    private final ProjectQueryService projectQueryService;

    public ProjectRouter(
            ProjectMakerService projectMakerService,
            ProjectQueryService projectQueryService) {

        this.projectMakerService = projectMakerService;
        this.projectQueryService = projectQueryService;
        super("/project");
    }

    /**
     * 프로젝트 생성 api
     * @author 김민규
     * @param userId 유저 Id
     * @param dto 프로젝트 생성 dto
     * @return 프로젝트 id
     */
    @Post()
    public IdResponse createProject(@Depends(CurrentUser.class) Long userId, CreateProjectRequest dto) {
        return projectMakerService.createProject(userId, dto);
    }

    @Get("/{project_id}")
    public SingleDataResponse<ProjectData> getProject(@Path("project_id") Long projectId) {
        return projectQueryService.getProject(projectId);
    }

    @Get()
    public DataCollectionResponse<ProjectData> getAllProjects(@Query("limit") Integer limit) {
        return projectQueryService.getAllProject(limit);
    }


    @Get("/search")
    public DataCollectionResponse<ProjectData> getFilterProjects(
            @Query("title") String title,
            @Query("maker_name") String makerName,
            @Query("category_name") String categoryName,
            @Query("state_names") Optional<List<String>> stateList,
            @Query("limit") Optional<Integer> limit,
            @Query("search_type") Optional<String> searchType) {

        return projectQueryService.getAllProject(
                title,
                makerName,
                categoryName,
                stateList,
                limit,
                searchType
        );
    }

    @Patch("/{project_id}")
    public IdResponse updateProject(@Depends(CurrentUser.class) Long userId, @Path("{project_id}") long projectId, EditProjectRequest dto) {
        return projectMakerService.editProject(userId, projectId, dto);
    }

    @Delete("/my/{project_id}")
    public DeleteResponse deleteProject(@Depends(CurrentUser.class) Long userId, @Path("{project_id}") long projectId) {
        return projectMakerService.deleteMakerSelfProject(userId, projectId);
    }

}
