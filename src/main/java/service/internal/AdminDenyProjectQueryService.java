package service.internal;

import model.dto.DenyProjectData;
import model.dto.ProjectData;
import model.dto.response.DataCollectionResponse;
import service.BaseService;

/**
 * 관리자가 거절된 펀딩을 조회하는 서비스 클래스
 * @since 2026-11-09
 * @author 김민규
 * @version 1.0
**/
public class AdminDenyProjectQueryService extends BaseService {

    public DataCollectionResponse<DenyProjectData> getDenyProject(Long reviewId) {
        return null;
    }

    public DataCollectionResponse<DenyProjectData> getAllDenyProject(int limit) {
        return null;
    }

}
