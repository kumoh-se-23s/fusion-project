package service.internal;

import constant.ProjectState;
import exception.*;
import lombok.RequiredArgsConstructor;
import model.Serializer;
import model.domain.*;
import model.dto.request.project.AdminProjectRequest;
import model.dto.response.DeleteResponse;
import model.dto.response.IdResponse;
import repository.*;
import service.BaseService;

import java.util.List;
import java.util.Map;


/**
 * 관리자 권한으로 프로젝트를 관리하는 서비스 클래스
 * @since 2026-11-08 완성
 * @author 김민규
 * @version 1.0
**/
@RequiredArgsConstructor
public class AdminProjectControlService extends BaseService {
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ProjectLikeRepository projectLikeRepository;

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
    public IdResponse createProject(AdminProjectRequest request) {
        return tx.execute(false, () -> {

            AppUser maker = userRepository.findById(request.getMakerId())
                    .orElseThrow(NotFoundException::new);


            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(NotFoundException::new);

            Project project = Serializer.toDomain(request, Project.class)
                    .toBuilder()
                    .maker(maker)
                    .category(category)
                    .build();


            if (project.getEndDate() == null || !project.getEndDate().isAfter(project.getStartDate())) {
                throw new OutOfRangeException("endDate must be after startDate");
            }

            return new IdResponse(projectRepository.create(project).getId());
        });
    }


    public IdResponse editProject(Long projectId, AdminProjectRequest request) {
        return tx.execute(false, () -> {
            Map<String, Object> dtoMap = request.getRequest().toUpdateMap();
            dtoMap.put("category", request.getCategoryId());
            dtoMap.put("maker", request.getMakerId());
            return new IdResponse(
                    projectRepository.update(projectId, dtoMap).
                        orElseThrow(NotFoundException::new).getId()
            );
        });
    }
    public DeleteResponse deleteProject(Long projectId) {
        return tx.execute(false, () -> {
            if (projectRepository.isExistById(projectId)) {
                return new DeleteResponse(projectRepository.delete(projectId));
            } else {
                return new DeleteResponse(false);
            }
        });
    }

    public DeleteResponse deleteProjectAllLikes(Long projectId) {
        return tx.execute(() -> {
            boolean result = true;
            List<ProjectLike> projectLikeList = projectLikeRepository.findByProject(projectId);
            for (ProjectLike projectLike : projectLikeList) {
                updateLikeValue(projectLike.getProject(), projectLike.getLikeType(), -1);
                if (!projectLikeRepository.delete(projectLike.getId())) {
                    result = false;
                }
            }
            return new DeleteResponse(result);
        });
    }

    private void updateLikeValue(Project project, Boolean likeType, int change) {
        if (likeType) {
            projectRepository.updateByLike(project.getId(), Math.max(project.getLikeCount() + change, 0));
        } else {
            projectRepository.updateByDislike(project.getId(), Math.max(project.getDislikeCount() + change,0));
        }
    }
}
