package service.external;

import constant.ProjectSearchType;
import constant.ProjectState;
import lombok.RequiredArgsConstructor;
import model.Serializer;
import model.domain.AppUser;
import model.domain.Category;
import model.domain.Project;
import model.dto.ProjectData;
import model.dto.request.project.GetProjectByCategoryAndStateRequest;
import model.dto.response.DataCollectionResponse;
import repository.*;
import service.BaseService;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

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
    public DataCollectionResponse<ProjectData> getAllProject(
            String title,
            String makerNickName,
            String categoryName,
            List<String> stateList,
            int limit,
            String searchType) {
        return tx.execute(true, () -> {
            ProjectSearchType projectSearchType = ProjectSearchType.fromString(searchType);

            List<ProjectState> projectStateList;

            if (!projectSearchType.getProjectStateList().isEmpty()) {
                projectStateList = projectSearchType.getProjectStateList();

            } else {
                projectStateList = ProjectState.fromStringList(stateList).stream()
                        .filter(ALLOWED_QUERY_STATE::contains)
                        .toList();

            }

            Order order = projectSearchType.getOrder();

            AppUser maker = userRepository.findByNickName(makerNickName)
                    .getFirst();

            Category category = categoryRepository.findByName(List.of(categoryName))
                    .getFirst();

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


}
