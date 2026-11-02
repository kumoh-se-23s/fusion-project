package service.external;

import constant.ProjectState;
import exception.NotFoundException;
import exception.NotNullException;
import exception.OutOfRangeException;
import lombok.RequiredArgsConstructor;
import model.Serializer;
import model.domain.*;
import model.dto.ProjectData;
import model.dto.request.project.CreateProjectRequest;
import model.dto.request.project.GetProjectByCategoryAndStateRequest;
import model.dto.response.DataCollectionResponse;
import model.dto.response.IdResponse;
import model.dto.response.project.ProjectInfoResponse;
import model.dto.response.reward.RewardInfoResponse;
import model.dto.response.reward.RewardUserResponse;
import model.dto.response.project.ProjectRewardResponse;
import model.dto.response.user.UserAddressResponse;
import repository.*;
import service.BaseService;

import java.util.*;

@RequiredArgsConstructor
public class ProjectService extends BaseService {
    private final ProjectRepository projectRepository;
    private final DonateRepository donateRepository;
    private final RewardRepository rewardRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

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

    //6. 전체 프로젝트 조회
    public DataCollectionResponse<ProjectData> getAllProject() {
        return tx.execute(true, () -> {
            List<ProjectData> dtos = new ArrayList<>();
            for (Project project : projectRepository.findAllOrderByCreatedAt()) {
                dtos.add(Serializer.toData(project, ProjectData.class));
            }

            return DataCollectionResponse.<ProjectData>builder()
                    .datas(dtos)
                    .amount((long) dtos.size())
                    .build();
        });
    }

    //7. 프로젝트 필터링. 목록이 null이거나 비어 있으면 그 조건은 적용하지 않는다
    public DataCollectionResponse<ProjectData> getByCategoryAndState(GetProjectByCategoryAndStateRequest dto)  {
        return tx.execute(true, () -> {

            List<ProjectState> stateList = dto.getProjectStateList();
            List<String> categoryNames = dto.getCategoryList();
            List<Category> categoryIdList = categoryRepository.findByName(categoryNames);

            List<Project> projectList = projectRepository.findByStateAndCategory(stateList, categoryIdList);
            return DataCollectionResponse.<ProjectData>builder()
                    .datas(Serializer.toDataList(projectList, ProjectData.class))
                    .amount((long) projectList.size())
                    .build();
        });
    }

}
