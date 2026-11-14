package controller.internal;

import model.dto.DenyProjectData;
import model.dto.request.deny_project.DenyProjectRequest;
import model.dto.response.DataCollectionResponse;
import model.dto.response.DeleteResponse;
import model.dto.response.IdResponse;
import model.dto.response.SingleDataResponse;
import service.internal.AdminDenyProjectControlService;
import service.internal.AdminDenyProjectQueryService;

public class AdminDenyProjectRouter extends BaseRouter {
    private final AdminDenyProjectControlService adminDenyProjectControlService;
    private final AdminDenyProjectQueryService adminDenyProjectQueryService;

    public AdminDenyProjectRouter(
            AdminDenyProjectControlService adminDenyProjectControlService,
            AdminDenyProjectQueryService adminDenyProjectQueryService) {
        this.adminDenyProjectControlService = adminDenyProjectControlService;
        this.adminDenyProjectQueryService = adminDenyProjectQueryService;
        super("internal/project/deny");
    }

    @Post("/{project_id}")
    public IdResponse createDenyProject(@Path("project_id") long projectId, DenyProjectRequest dto) {
        return adminDenyProjectControlService.createDenyProject(projectId, dto);
    }

    @Get("/{deny_project_id}")
    public SingleDataResponse<DenyProjectData> getDenyProject(@Path("deny_project_id") long denyProjectId) {
        return adminDenyProjectQueryService.getDenyProject(denyProjectId);

    }

    @Get()
    public DataCollectionResponse<DenyProjectData> getAllDenyProject(int limit) {
        return adminDenyProjectQueryService.getAllDenyProject(limit);
    }

    @Get("/{project_id}")
    public DataCollectionResponse<DenyProjectData> getAllDenyProjectByProject(@Path("project_id") long projectId) {
        return adminDenyProjectQueryService.getDenyProjectByProject(projectId);
    }

    @Patch("/{deny_project_id}")
    public IdResponse updateDenyProject(@Path("deny_project_id") long denyProjectId, DenyProjectRequest dto) {
        return adminDenyProjectControlService.editDenyProject(denyProjectId, dto);
    }

    @Delete("/deny_project_id}")
    public DeleteResponse deleteDenyProject(@Path("deny_project_id") long denyProjectId) {
        return adminDenyProjectControlService.deleteDenyProject(denyProjectId);
    }
}
