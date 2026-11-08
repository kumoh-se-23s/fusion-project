package service.internal;

import lombok.RequiredArgsConstructor;
import model.dto.ProjectData;
import model.dto.response.DataCollectionResponse;
import service.BaseService;

/**
 * 관리자 권한으로 프로젝트를 조회하는 서비스 클래스
 * @since 2026-11-08 완성
 * @author 김민규
 * @version 1.0
**/

@RequiredArgsConstructor
public class AdminProjectQueryService extends BaseService {
    public DataCollectionResponse<ProjectData> getProject(Long reviewId) {
        return null;
    }

    public DataCollectionResponse<ProjectData> getAllProject(int limit) {
        return null;
    }

    public DataCollectionResponse<ProjectData> getDisapproveProject(int limit) {
        return null;
    }
}
