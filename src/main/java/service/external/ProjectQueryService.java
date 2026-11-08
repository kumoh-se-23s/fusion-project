package service.external;

import constant.ProjectState;
import lombok.RequiredArgsConstructor;
import model.Serializer;
import model.domain.Category;
import model.domain.Project;
import model.dto.ProjectData;
import model.dto.request.project.GetProjectByCategoryAndStateRequest;
import model.dto.response.DataCollectionResponse;
import repository.CategoryRepository;
import repository.ProjectRepository;
import service.BaseService;

import java.util.ArrayList;
import java.util.List;

/**
 * 일반유저가 프로젝트룰 조회하는 서비스 클래스
 * @since 2026-11-0? 완성
 * @author 김민규
 * @version 1.0
**/
@RequiredArgsConstructor
public class ProjectQueryService extends BaseService {
    private final ProjectRepository projectRepository;
    private final CategoryRepository categoryRepository;

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
