package service.internal;

import constant.ProjectState;
import exception.NotFoundException;
import exception.NotPendingProjectException;
import lombok.RequiredArgsConstructor;
import model.domain.Project;
import model.dto.response.IdResponse;
import repository.ProjectRepository;
import service.BaseService;

@RequiredArgsConstructor
public class AdminProjectControlService extends BaseService {
    private final ProjectRepository projectRepository;

    //4. 관리자가 메이커의 프로젝트 승인
    public IdResponse updateProjectApprove(Long projectId) {
        return tx.execute(false, () -> {
            Project project = projectRepository.findById(projectId)
                    .orElseThrow(NotFoundException::new);
            if (project.getState() != ProjectState.PENDING_APPROVAL) {
                throw new NotPendingProjectException();
            }
            Project approved = projectRepository.updateByApprove(project.getId())
                    .orElseThrow(NotFoundException::new);
            return new IdResponse(approved.getId());
        });
    }
}
