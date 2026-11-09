package service.internal;

import constant.ProjectState;
import exception.NotFoundException;
import model.domain.DenyProject;
import model.domain.Project;
import model.dto.request.deny_project.DenyProjectRequest;
import model.dto.response.DeleteResponse;
import model.dto.response.IdResponse;
import repository.DenyProjectRepository;
import repository.ProjectRepository;
import service.BaseService;

import java.util.Map;

/**
 * 관리자가 거절된 펀딩을 관리하는 서비스 클래스
 * @since 2026-11-09
 * @author 김민규
 * @version 1.0
**/
public class AdminDenyProjectControlService extends BaseService {
    private ProjectRepository projectRepository;
    private DenyProjectRepository denyProjectRepository;

    public IdResponse createDenyProject(Long projectId, DenyProjectRequest dto) {
        return tx.execute(false, () -> {
            Project project = projectRepository.findById(projectId)
                    .orElseThrow(NotFoundException::new);

            DenyProject denyProject = DenyProject.builder()
                    .project(project)
                    .reason(dto.getDenyReason())
                    .build();

            projectRepository.update(
                    projectId,
                    Map.of("state", ProjectState.REJECTED)
            );

            return new IdResponse(
                denyProjectRepository.create(denyProject)
                    .getId()
            );
        });
    }

    public IdResponse editDenyProject(Long denyProjectId, DenyProjectRequest dto) {
        return tx.execute(false, () -> {
            DenyProject denyProject =  denyProjectRepository.update(denyProjectId, dto.toUpdateMap())
                        .orElseThrow(NotFoundException::new);

            projectRepository.update(
                    denyProject.getProject().getId(),
                    Map.of("state", ProjectState.REJECTED)
            );

            return new IdResponse(denyProject.getId());
        });
    }

    public DeleteResponse deleteDenyProject(Long denyProjectId) {
        return tx.execute(false, () -> {
            DenyProject denyProject = denyProjectRepository.findById(denyProjectId)
                    .orElseThrow(NotFoundException::new);

            return new DeleteResponse(denyProjectRepository.delete(denyProjectId));
        });
    }
}
