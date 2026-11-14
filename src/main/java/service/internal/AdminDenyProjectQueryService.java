package service.internal;

import exception.NotFoundException;
import model.Serializer;
import model.domain.DenyProject;
import model.dto.DenyProjectData;
import model.dto.response.DataCollectionResponse;
import model.dto.response.SingleDataResponse;
import repository.DenyProjectRepository;
import repository.ProjectRepository;
import service.BaseService;

import java.util.List;

/**
 * 관리자가 거절된 펀딩을 조회하는 서비스 클래스
 * @since 2026-11-09
 * @author 김민규
 * @version 1.0
**/
public class AdminDenyProjectQueryService extends BaseService {
    private DenyProjectRepository denyProjectRepository;
    private ProjectRepository projectRepository;

    public SingleDataResponse<DenyProjectData> getDenyProject(Long denyProjectId) {
        DenyProject denyProject = denyProjectRepository.findById(denyProjectId)
                .orElseThrow(NotFoundException::new);

        return new SingleDataResponse<>(
                Serializer.toData(denyProject, DenyProjectData.class)
        );
    }

    public DataCollectionResponse<DenyProjectData> getAllDenyProject(int limit) {
        List<DenyProject> denyProjectList = denyProjectRepository.findAll(limit);

        return DataCollectionResponse.<DenyProjectData>builder()
                .datas(Serializer.toDataList(denyProjectList, DenyProjectData.class))
                .amount((long) denyProjectList.size())
                .build();
    }
    public DataCollectionResponse<DenyProjectData> getDenyProjectByProject(Long projectId) {
        List<DenyProject> denyProjectList = denyProjectRepository.findByProject(projectId);

        return DataCollectionResponse.<DenyProjectData>builder()
                .datas(Serializer.toDataList(denyProjectList, DenyProjectData.class))
                .amount((long) denyProjectList.size())
                .build();
    }

}
