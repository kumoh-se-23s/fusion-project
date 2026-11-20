package controller.internal;

import com.minihttp.core.route.BaseRouter;
import com.minihttp.core.route.annotation.*;
import model.dto.ProjectData;
import model.dto.request.project.AdminProjectRequest;
import model.dto.response.DataCollectionResponse;
import model.dto.response.DeleteResponse;
import model.dto.response.IdResponse;
import model.dto.response.SingleDataResponse;
import service.internal.AdminProjectControlService;
import service.internal.AdminProjectQueryService;

import java.util.List;

/**
 * 관리자 전용 프로젝트 라우터
 * @author 김민규
 * @since 2026-11-16
 * @version 1.0
 */
public class AdminProjectRouter extends BaseRouter {
    private final AdminProjectControlService adminProjectControlService;
    private final AdminProjectQueryService adminProjectQueryService;

    public AdminProjectRouter(
            AdminProjectControlService adminProjectControlService,
            AdminProjectQueryService adminProjectQueryService) {

        this.adminProjectControlService = adminProjectControlService;
        this.adminProjectQueryService = adminProjectQueryService;
        super("/internal/project");
    }

    @Post()
    public IdResponse createProject(AdminProjectRequest dto) {
        return adminProjectControlService.createProject(dto);
    }

    @Get("/{project_id}")
    public SingleDataResponse<ProjectData> getProject(@Path("project_id") Long projectId) {
        return adminProjectQueryService.getProject(projectId);
    }

    @Get()
    public DataCollectionResponse<ProjectData> getAllProjects(@Query(value = "limit", required = false) Integer limit) {
        return adminProjectQueryService.getAllProject(limit);
    }

    @Get("/pending")
    public DataCollectionResponse<ProjectData> getPendingProjects(@Query(value = "limit", required = false) Integer limit) {
        return adminProjectQueryService.getPendingProject(limit);
    }

    @Get("/search")
    public DataCollectionResponse<ProjectData> getFilterProjects(
        @Query(value = "title", required = false) String title,
        @Query(value = "maker_name", required = false) String makerName,
        @Query(value = "category_name", required = false) String categoryName,
        @Query(value = "state_names", required = false) List<String> stateList,
        @Query(value = "limit", required = false) Integer limit,
        @Query(value = "search_type", required = false) String searchType) {

        return adminProjectQueryService.getAllProject(
                title,
                makerName,
                categoryName,
                stateList,
                limit,
                searchType
        );
    }

    @Patch("/{project_id}")
    public IdResponse updateProject(@Path("{project_id}") long projectId, AdminProjectRequest dto) {
        return adminProjectControlService.editProject(projectId, dto);
    }

    @Patch("/approve/{project_id}")
    public IdResponse approveProject(@Path("{project_id") long projectId) {
        return adminProjectControlService.updateProjectApprove(projectId);
    }


    @Delete("/{project_id}")
    public DeleteResponse deleteProject(@Path("{project_id}") long projectId) {
        return adminProjectControlService.deleteProject(projectId);
    }

    @Delete("/like/{project_id}")
    public DeleteResponse deleteProjectLikes(@Path("{project_id}") long projectId) {
        return adminProjectControlService.deleteProjectAllLikes(projectId);
    }
}
