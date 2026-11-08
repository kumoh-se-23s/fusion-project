package service.internal;

import constant.ProjectState;
import exception.*;
import lombok.RequiredArgsConstructor;
import model.Serializer;
import model.domain.*;
import model.dto.DenyProjectData;
import model.dto.ProjectData;
import model.dto.request.project.AdminCreateProjectRequest;
import model.dto.request.project.CreateProjectRequest;
import model.dto.request.project.EditProjectRequest;
import model.dto.response.DataCollectionResponse;
import model.dto.response.DeleteResponse;
import model.dto.response.IdResponse;
import model.dto.response.project.ProjectInfoResponse;
import model.dto.response.project.ProjectRewardResponse;
import model.dto.response.reward.RewardInfoResponse;
import model.dto.response.reward.RewardUserResponse;
import model.dto.response.user.UserAddressResponse;
import repository.*;
import service.BaseService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

//    //2. 프로젝트 기본 정보 등록
//    public IdResponse createProject(Long makerId, AdminCreateProjectRequest request) {
//        return tx.execute(false, () -> {
//            AppUser maker = userRepository.findById(makerId)
//                    .orElseThrow(NotFoundException::new);
//            if (request.getCategoryId() == null) {
//                throw new NotNullException("Category Id is Null");
//            }
//            Category category = categoryRepository.findById(request.getCategoryId())
//                    .orElseThrow(NotFoundException::new);
//            //요청에서 비운 startDate, purposeAmount는 도메인 기본값을 유지하고 state는 JUST_USER_INPUT으로 시작
//            Project project = Serializer.toDomain(request, Project.class)
//                    .toBuilder()
//                    .maker(maker)
//                    .category(category)
//                    .build();
//
//            if (project.getPurposeAmount() == null || project.getPurposeAmount().signum() <= 0) {
//                throw new OutOfRangeException("purposeAmount must be greater than 0");
//            }
//            if (project.getEndDate() == null || !project.getEndDate().isAfter(project.getStartDate())) {
//                throw new OutOfRangeException("endDate must be after startDate");
//            }
//
//            return new IdResponse(projectRepository.create(project).getId());
//
//        });
//    }
//
//    public IdResponse editProject(Long projectId, EditProjectRequest request) {
//        return tx.execute(false, () -> {
//            Map<String, Object> dtoMap = request.getProjectDataDTO().toUpdateMap();
//            dtoMap.put("category", request.getCategoryId());
//
//            return new IdResponse(
//                    projectRepository.update(projectId, dtoMap).
//                        orElseThrow(NotFoundException::new).getId()
//            );
//        });
//    }
//    public DataCollectionResponse<ProjectData> getMakerProject(Long makerId) {
//        return tx.execute(true, () -> {
//            List<ProjectData> dtoList = Serializer.toDataList(projectRepository.findByUserId(makerId), ProjectData.class);
//            return DataCollectionResponse.<ProjectData>builder()
//                    .datas(dtoList)
//                    .amount((long) dtoList.size())
//                    .build();
//        });
//    }
//
//    public DeleteResponse deleteMakerSelfProject(Long makerId, Long projectId) {
//        return tx.execute(false, () -> {
//            Project project =  projectRepository.findById(projectId)
//                    .orElseThrow(NotFoundException::new);
//            if (project.getMaker().getId() != makerId) {
//                throw new InvalidMakerException();
//            }
//            return new DeleteResponse(projectRepository.delete(projectId));
//        });
//    }
//
//    public DataCollectionResponse<DenyProjectData> getAllDenyProjects(Long makerId) {
//        List<Project> projectList = projectRepository.findByUserId(makerId);
//
//        List<DenyProjectData> dtoList =
//                Serializer.toDataList(
//                        denyProjectRepository.findByProject(projectList),
//                        DenyProjectData.class
//                );
//
//        return DataCollectionResponse.<DenyProjectData>builder()
//                .datas(dtoList)
//                .amount((long)dtoList.size())
//                .build();
//    }

}

}
