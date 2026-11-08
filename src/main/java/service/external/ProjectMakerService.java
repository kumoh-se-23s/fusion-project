package service.external;

import exception.InvalidMakerException;
import exception.NotFoundException;
import exception.NotNullException;
import exception.OutOfRangeException;
import lombok.RequiredArgsConstructor;
import model.Serializer;
import model.domain.*;
import model.dto.DenyProjectData;
import model.dto.ProjectData;
import model.dto.request.project.CreateProjectRequest;
import model.dto.request.project.EditProjectRequest;
import model.dto.response.DataCollectionResponse;
import model.dto.response.DeleteResponse;
import model.dto.response.IdResponse;
import model.dto.response.project.ProjectInfoResponse;
import model.dto.response.reward.RewardInfoResponse;
import model.dto.response.reward.RewardUserResponse;
import model.dto.response.project.ProjectRewardResponse;
import model.dto.response.user.UserAddressResponse;
import repository.*;
import service.BaseService;

import java.util.*;

/**
 * 유저(메이커)가 프로젝트를 관리하는 서비스 클래스
 * @since 2026-11-05 완성
 * @author 김민규
 * @version 1.1
 * Edit에서 usre id 체크 기능 추가
**/
@RequiredArgsConstructor
public class ProjectMakerService extends BaseService {
    private final ProjectRepository projectRepository;
    private final DonateRepository donateRepository;
    private final RewardRepository rewardRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final DenyProjectRepository denyProjectRepository;

    //1.프로젝트의 리워드 정보와 리워드별 후원자 배송지
    public ProjectRewardResponse getProjectByReward(Long projectId) {
        return tx.execute(true, () -> {
            Project project = projectRepository.findById(projectId)
                    .orElseThrow(NotFoundException::new);

            List<Reward> rewardList = rewardRepository.findByProject(project);
            List<Donate> donateList = donateRepository.findByRewards(rewardList);
            List<RewardUserResponse> rewardUserResponses = new ArrayList<>();
            Map<Long, List<UserAddressResponse>> rewardIdSet = new HashMap<>();

            for (Donate donate : donateList) {
                //탈퇴한 사용자의 후원은 배송지가 없으므로 제외
                if (donate.getUser() == null) {
                    continue;
                }
                UserAddressResponse UserAddressDTO =
                    Serializer.toData(donate.getUser(), UserAddressResponse.class);

                rewardIdSet.computeIfAbsent(donate.getReward().getId(), k -> new ArrayList<>())
                    .add(UserAddressDTO);
            }
            for (Reward reward : rewardList) {
                rewardUserResponses.add(
                        RewardUserResponse.builder()
                            .rewardInfoResponse(Serializer.toData(reward, RewardInfoResponse.class))
                            .userAddressInfo(rewardIdSet.getOrDefault(reward.getId(), List.of()))
                            .build()
                );
            }
            return ProjectRewardResponse.builder()
                .projectInfo(Serializer.toData(project, ProjectInfoResponse.class))
                .rewards(rewardUserResponses)
                .build();
        });
    }

    //2. 프로젝트 기본 정보 등록
    public IdResponse createProject(Long makerId, CreateProjectRequest request) {
        return tx.execute(false, () -> {
            AppUser maker = userRepository.findById(makerId)
                    .orElseThrow(NotFoundException::new);
            if (request.getCategoryId() == null) {
                throw new NotNullException("Category Id is Null");
            }
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(NotFoundException::new);
            //요청에서 비운 startDate, purposeAmount는 도메인 기본값을 유지하고 state는 JUST_USER_INPUT으로 시작
            Project project = Serializer.toDomain(request, Project.class)
                    .toBuilder()
                    .maker(maker)
                    .category(category)
                    .build();

            if (project.getPurposeAmount() == null || project.getPurposeAmount().signum() <= 0) {
                throw new OutOfRangeException("purposeAmount must be greater than 0");
            }
            if (project.getEndDate() == null || !project.getEndDate().isAfter(project.getStartDate())) {
                throw new OutOfRangeException("endDate must be after startDate");
            }

            return new IdResponse(projectRepository.create(project).getId());

        });
    }

    public IdResponse editProject(Long makerId, Long projectId, EditProjectRequest request) {
        return tx.execute(false, () -> {
            Project project =  projectRepository.findById(projectId)
                    .orElseThrow(NotFoundException::new);

            if (!Objects.equals(project.getMaker().getId(), makerId)) {
                throw new InvalidMakerException();
            }

            Map<String, Object> dtoMap = request.getProjectDataDTO().toUpdateMap();
            dtoMap.put("category", request.getCategoryId());


            return new IdResponse(
                    projectRepository.update(projectId, dtoMap).
                        orElseThrow(NotFoundException::new).getId()
            );
        });
    }
    public DataCollectionResponse<ProjectData> getMakerProject(Long makerId) {
        return tx.execute(true, () -> {
            List<ProjectData> dtoList = Serializer.toDataList(projectRepository.findByUserId(makerId), ProjectData.class);
            return DataCollectionResponse.<ProjectData>builder()
                    .datas(dtoList)
                    .amount((long) dtoList.size())
                    .build();
        });
    }

    public DeleteResponse deleteMakerSelfProject(Long makerId, Long projectId) {
        return tx.execute(false, () -> {
            Project project =  projectRepository.findById(projectId)
                    .orElseThrow(NotFoundException::new);

            if (!Objects.equals(project.getMaker().getId(), makerId)) {
                throw new InvalidMakerException();
            }

            return new DeleteResponse(projectRepository.delete(projectId));
        });
    }

    public DataCollectionResponse<DenyProjectData> getAllDenyProjects(Long makerId) {
        List<Project> projectList = projectRepository.findByUserId(makerId);

        List<DenyProjectData> dtoList =
                Serializer.toDataList(
                        denyProjectRepository.findByProject(projectList),
                        DenyProjectData.class
                );

        return DataCollectionResponse.<DenyProjectData>builder()
                .datas(dtoList)
                .amount((long)dtoList.size())
                .build();
    }
}
