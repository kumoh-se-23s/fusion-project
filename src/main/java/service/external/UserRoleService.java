package service.external;

import constant.Role;
import lombok.RequiredArgsConstructor;
import model.domain.UserRoleRelation;
import model.dto.response.MyRoleResponse;
import repository.UserRoleRelationRepository;
import service.BaseService;

import java.util.List;

@RequiredArgsConstructor
public class UserRoleService extends BaseService {
    private final UserRoleRelationRepository userRoleRelationRepository;

    public MyRoleResponse getCurrentUserPermissions(long userId) {
        List<UserRoleRelation> relations = userRoleRelationRepository.findByUserId(userId);

        List<Role> userRoleList = relations.stream().map(r -> r.getRole().getRole()).toList();

        return new MyRoleResponse(userRoleList);
    }
}
