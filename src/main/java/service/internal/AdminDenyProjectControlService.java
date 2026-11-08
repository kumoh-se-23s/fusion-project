package service.internal;

import model.dto.response.DeleteResponse;
import model.dto.response.IdResponse;
import service.BaseService;

/**
 * 관리자가 거절된 펀딩을 관리하는 서비스 클래스
 * @since 2026-11-09
 * @author 김민규
 * @version 1.0
**/
public class AdminDenyProjectControlService extends BaseService {

    public IdResponse createDenyProject(Long projectId) {
        return null;
    }
    //편의상 모든 api는 펀딩 id로 진행  or 일관성, 효율성을 위해 거절 펀딩 id로 진행
    public IdResponse editDenyProject(Long projectId) {
        return null;
    }

    public DeleteResponse deleteDenyProject(Long projectId) {
        return null;
    }
}
