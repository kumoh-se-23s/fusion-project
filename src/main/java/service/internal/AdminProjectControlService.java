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
    private final UserRoleRelationRepository userRoleRelationRepository;

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
//            UserRoleRelation userRoleRelation = UserRoleRelation.builder()
            return new IdResponse(approved.getId());
        });
    }
    public IdResponse createProject(AdminProjectRequest request) {
        return tx.execute(false, () -> {

            AppUser maker = userRepository.findById(request.getMakerId())
                    .orElseThrow(NotFoundException::new);


            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(NotFoundException::new);

            Project project = Serializer.toDomain(request.getRequest(), Project.class)
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
            if (request.getCategoryId() != null) {
                Category category = categoryRepository.findById(request.getCategoryId())
                        .orElseThrow(NotFoundException::new);

                dtoMap.put("category", request.getCategoryId());
            }
            if (request.getMakerId() != null) {
                AppUser appUser = userRepository.findById(request.getMakerId())
                        .orElseThrow(NotFoundException::new);

                dtoMap.put("maker", request.getMakerId());
            }
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
            Project project = projectRepository.findById(projectId)
                    .orElseThrow(NotFoundException::new);
            List<ProjectLike> projectLikeList = projectLikeRepository.findByProject(projectId);
            Long projectLikeCount = project.getLikeCount();
            Long projectDislikeCount = project.getDislikeCount();
            for (ProjectLike projectLike : projectLikeList) {
                Boolean projectType = projectLike.getLikeType();

                if (!projectLikeRepository.delete(projectLike.getId())) {
                    result = false;
                } else {
                    if (projectType) {
                        projectLikeCount -= 1;
                    } else {
                        projectDislikeCount -= 1;
                    }
                }
            }
            projectRepository.updateByLike(project.getId(), projectLikeCount)
                    .orElseThrow(NotFoundException::new);
            projectRepository.updateByDislike(project.getId(), projectDislikeCount)
                    .orElseThrow(NotFoundException::new);

            return new DeleteResponse(result);
        });
    }

}
