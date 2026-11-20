package service.external;

import constant.ProjectSearchType;
import constant.ProjectState;
import exception.InvalidStateException;
import exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import model.Serializer;
import model.domain.*;
import model.dto.ProjectData;
import model.dto.response.DataCollectionResponse;
import model.dto.response.SingleDataResponse;
import repository.*;
import service.BaseService;

import java.util.*;

/**
 * 일반유저가 프로젝트룰 조회하는 서비스 클래스
 * @since 2026-11-08
 * @author 김민규
 * @version 1.1
 *
 * @version 1.0 기본 조회 기능 추가
 * @version 1.1 전체 필터링 조회 기능 추가
**/
@RequiredArgsConstructor
public class ProjectQueryService extends BaseService {
    private final ProjectRepository projectRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final DonateRepository donateRepository;

    private final static Set<ProjectState> ALLOWED_QUERY_STATE =
            EnumSet.of(
                    ProjectState.APPROVED,
                    ProjectState.IN_PROGRESS,
                    ProjectState.COMPLETED,
                    ProjectState.FAILED,
                    ProjectState.CANCELED
            );

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
    public DataCollectionResponse<ProjectData> getAllProject(
            String title,
            String makerNickName,
            String categoryName,
            List<String> stateList,
            Integer limit,
            String searchType) {
        return tx.execute(true, () -> {
            ProjectSearchType projectSearchType = ProjectSearchType.fromString(searchType);
            List<ProjectState> projectStateList = ProjectState.fromStringList(stateList);

            if (!ALLOWED_QUERY_STATE.containsAll(projectStateList)) {
                throw new InvalidStateException();
            }
            if (!projectSearchType.getProjectStateList().isEmpty()) {
                if (!projectSearchType.getProjectStateList().containsAll(projectStateList)) {
                    throw new InvalidStateException();
                }
            }

            Order order = projectSearchType.getOrder();
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

            List<Project> projectList = projectRepository.findByFilter(
                title,
                maker,
                category,
                projectStateList,
                limit,
                Sort.by(order)
            );

            return DataCollectionResponse.<ProjectData>builder()
                    .datas(Serializer.toDataList(projectList, ProjectData.class))
                    .amount((long) projectList.size())
                    .build();
        });
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
    public DataCollectionResponse<ProjectData> getUserDonateProject(Long userId) {
        List<Donate> userDonateList = donateRepository.findByUserId(userId);

        List<Project> projectList = userDonateList.stream()
                .map(donate -> donate.getReward().getProject())
                .toList();

        return DataCollectionResponse.<ProjectData>builder()
                .datas(Serializer.toDataList(projectList, ProjectData.class))
                .amount((long) projectList.size())
                .build();
    }


}
