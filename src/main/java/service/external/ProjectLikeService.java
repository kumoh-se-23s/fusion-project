package service.external;

import exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import model.domain.AppUser;
import model.domain.Project;
import model.domain.ProjectLike;
import model.dto.request.like.LikeRequest;
import model.dto.response.DeleteResponse;
import model.dto.response.IdResponse;
import repository.ProjectLikeRepository;
import repository.ProjectRepository;
import repository.UserRepository;
import service.BaseService;

import java.util.function.BiConsumer;

/**
 * 프로젝트의 좋아요를 관리하는 서비스 클래스
 * @since 2026-11-05 완성
 * @author 김민규
 * @version 1.0
**/
@RequiredArgsConstructor
public class ProjectLikeService extends BaseService {

    private final ProjectLikeRepository projectLikeRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public IdResponse createProjectLike(Long projectId, Long userId, LikeRequest request) {
        return tx.execute(() -> {
            Project project = projectRepository.findById(projectId)
                    .orElseThrow(NotFoundException::new);

            AppUser user = userRepository.findById(userId)
                    .orElseThrow(NotFoundException::new);

            updateLikeValue(project, request.getLikeType(), 1);

            return new IdResponse(
                    projectLikeRepository.create(
                            ProjectLike.builder()
                                    .likeType(request.getLikeType())
                                    .project(project)
                                    .user(user)
                                    .build()
                    ).getId()

            );

        });
    }

    public IdResponse editProjectLike(Long projectId, Long userId, LikeRequest request) {
        return tx.execute(() -> {
            ProjectLike projectLike = projectLikeRepository.findByProjectAndUser(projectId, userId).getFirst();

            Project project = updateLikeValue(projectLike.getProject(), projectLike.getLikeType(), -1);
            project = updateLikeValue(project, request.getLikeType(), 1);

            projectLike =  projectLikeRepository.update(project.getId(), request.toUpdateMap())
                            .orElseThrow(NotFoundException::new);

            return new IdResponse(projectLike.getId());
        });
    }
    public DeleteResponse deleteProjectLike(Long projectId, Long userId) {
        return tx.execute(() -> {
            ProjectLike projectLike = projectLikeRepository.findByProjectAndUser(projectId, userId).getFirst();

            updateLikeValue(projectLike.getProject(), projectLike.getLikeType(), -1);

            return new DeleteResponse(projectLikeRepository.delete(projectLike.getId()));
        });
    }

    private Project updateLikeValue(Project project, Boolean likeType, int change) {
        Long current = likeType ? project.getLikeCount() : project.getDislikeCount();
        current += change;

        if (likeType) {
            return projectRepository.updateByLike(project.getId(), Math.max(current, 0L))
                    .orElseThrow(NotFoundException::new);
        } else {
            return projectRepository.updateByDislike(project.getId(), Math.max(current, 0L))
                    .orElseThrow(NotFoundException::new);
        }
    }
}

