package service.internal;

import constant.AdminProjectSearchType;
import constant.ProjectState;
import exception.InvalidSearchTypeException;
import exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import model.Serializer;
import model.domain.*;
import model.dto.ProjectData;
import model.dto.response.DataCollectionResponse;
import model.dto.response.SingleDataResponse;
import repository.*;
import service.BaseService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 관리자 권한으로 프로젝트를 조회하는 서비스 클래스
 * @since 2026-11-08
 * @author 김민규
 * @version 1.1
 *
 * @version 1.0 기본 조회 메소드 추가
 * @version 1.1 전체 조회 조건 추가
**/

@RequiredArgsConstructor
public class AdminProjectQueryService extends BaseService {
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final CategoryRepository categoryRepository;
    private final RewardRepository rewardRepository;
    private final DonateRepository donateRepository;

    public DataCollectionResponse<ProjectData> getAllProject(
            String title,
            String makerNickName,
            String categoryName,
            List<String> stateList,
            Integer limit,
            String searchType) {
        return tx.execute(true, () -> {
            Integer limitValue = limit;
            AdminProjectSearchType projectSearchType = AdminProjectSearchType.fromString(searchType);
            List<ProjectState> projectStateList = ProjectState.fromStringList(stateList);

            Optional<Order> order = projectSearchType.getOrder();

            AppUser maker = null;
            if (makerNickName != null && !makerNickName.isBlank()) {
                maker = userRepository.findByNickName(makerNickName)
                            .stream()
                            .findFirst()
                            .orElseThrow(NotFoundException::new);
            }

            Category category = null;
            if (categoryName != null && !categoryName.isBlank()) {
                category = categoryRepository.findByName(categoryName)
                    .stream()
                    .findFirst()
                    .orElseThrow(NotFoundException::new);
            }
            boolean isAchievementRate =
                    projectSearchType == AdminProjectSearchType.ASC_ACHIEVEMENT_RATE
                    || projectSearchType == AdminProjectSearchType.DESC_ACHIEVEMENT_RATE;

            boolean isSupporterCount = projectSearchType == AdminProjectSearchType.DESC_SUPPORTER_COUNT
                        || projectSearchType == AdminProjectSearchType.ASC_SUPPORTER_COUNT;
            if (isSupporterCount || isAchievementRate) {
                limitValue = null;
            }
            List<Project> projectList = projectRepository.findByFilter(
                    title,
                    maker,
                    category,
                    projectStateList,
                    limitValue,
                    order.map(Sort::by).orElse(Sort.defaultSort()));

            if (projectList.isEmpty()) {
                throw new NotFoundException();
            }



            if (order.isEmpty()) {
                if (isAchievementRate) {
                    projectList = sortByAchievementRate(projectList, projectSearchType, limit);
                } else if (isSupporterCount) {
                    projectList = sortBySupporterCount(projectList, projectSearchType, limit);
                } else {
                    throw new InvalidSearchTypeException("Not Implement Search Type");
                }
            }

            return DataCollectionResponse.<ProjectData>builder()
                    .datas(Serializer.toDataList(projectList, ProjectData.class))
                    .amount((long) projectList.size())
                    .build();
        });
    }
    private BigDecimal achievementRate(Project project) {
        BigDecimal purpose = project.getPurposeAmount();
        if (purpose == null || purpose.signum() == 0) {
            return BigDecimal.ZERO;
        }
        return project.getDonateAmount().divide(purpose, 4, RoundingMode.HALF_UP);
    }

    private List<Project> sortByAchievementRate(List<Project> projectList, AdminProjectSearchType projectSearchType, Integer limit) {
        Comparator<Project> comparator = Comparator.comparing(this::achievementRate);

        if (projectSearchType == AdminProjectSearchType.DESC_ACHIEVEMENT_RATE) {
            comparator = comparator.reversed();
        }
        if (limit == null) {
            limit = projectList.size();
        }
        return projectList.stream()
                .sorted(comparator)
                .limit(limit)
                .toList();
    }

    private List<Project> sortBySupporterCount(List<Project> projectList, AdminProjectSearchType projectSearchType, Integer limit) {
        List<Reward> rewardList = rewardRepository.findByProjects(projectList);
        List<Donate> donateList = donateRepository.findByRewards(rewardList);

        Map<Long, Long> countMap = donateList.stream()
                .collect(Collectors.groupingBy(
                        d -> d.getReward().getProject().getId(),
                        Collectors.counting()));

        Comparator<Project> comparator = Comparator.comparing(
                (Project p) -> countMap.getOrDefault(p.getId(), 0L));

        if (projectSearchType == AdminProjectSearchType.DESC_SUPPORTER_COUNT) {
            comparator = comparator.reversed();
        }
        if (limit == null) {
            limit = projectList.size();
        }
        return projectList.stream()
                .sorted(comparator)
                .limit(limit)
                .toList();
    }

    public SingleDataResponse<ProjectData> getProject(Long projectId) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(NotFoundException::new);

        return new SingleDataResponse<>(
            Serializer.toData(project, ProjectData.class)
        );
    }

    public DataCollectionResponse<ProjectData> getAllProject(Integer limit) {
        List<Project> projectList = projectRepository.findAll(limit);

        return DataCollectionResponse.<ProjectData>builder()
                .datas(Serializer.toDataList(projectList, ProjectData.class))
                .amount((long) projectList.size())
                .build();
    }

    public DataCollectionResponse<ProjectData> getPendingProject(Integer limit) {
        List<Project> projectList = projectRepository.findByPending(limit);

        return DataCollectionResponse.<ProjectData>builder()
                .datas(Serializer.toDataList(projectList, ProjectData.class))
                .amount((long) projectList.size())
                .build();
    }
}
