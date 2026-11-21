package service.internal;

import constant.Role;
import exception.AlreadyExistException;
import exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import model.domain.AppUser;
import model.domain.UserRole;
import model.domain.UserRoleRelation;
import model.dto.response.DeleteResponse;
import model.dto.response.IdResponse;
import model.dto.response.UserRole.UserRoleResponse;
import repository.UserRepository;
import repository.UserRoleRelationRepository;
import repository.UserRoleRepository;
import service.BaseService;

import java.util.List;

@RequiredArgsConstructor
public class AdminUserRoleService extends BaseService {
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserRoleRelationRepository userRoleRelationRepository;

    /**
     * 유저 권한 삭제
     *
     * @param userId
     * @param roleName
     * @return DeleteResponse
     */
    public DeleteResponse removeRole(long userId, Role roleName) {
        return tx.execute(() -> {
            UserRole role = userRoleRepository.findByRole(roleName).orElseThrow(NotFoundException::new);

            UserRoleRelation relation = userRoleRelationRepository.findByUserAndRole(userId, role.getId()).orElseThrow(NotFoundException::new);

            boolean result = userRoleRelationRepository.delete(relation.getId());

            return new DeleteResponse(result);
        });
    }

    /**
     * 유저 권한 부여
     *
     * @param userId
     * @param roleName
     * @return IdResponse
     */
    public IdResponse assignRole(long userId, Role roleName) {
        long relationId = tx.execute(() -> {
            AppUser user = userRepository.findById(userId).orElseThrow(NotFoundException::new);

            UserRole role = userRoleRepository.findByRole(roleName).orElseThrow(NotFoundException::new);

            if (userRoleRelationRepository.findByUserAndRole(userId, role.getId()).isPresent()) {
                throw new AlreadyExistException();
            }

            UserRoleRelation relation = UserRoleRelation.builder()
                    .user(user)
                    .role(role)
                    .build();

            return userRoleRelationRepository.create(relation).getId();

        });

        return new IdResponse(relationId);
    }

    /**
     * 유저 권한 검색 <br>
     * 있는 유저면 성공 없으면 실패
     * @param userId
     * @return UserRoleResponse
     */
    public UserRoleResponse getRoleByUser(long userId) {
        return tx.execute(() -> {
            if (!userRepository.isExistById(userId)) {
                throw new NotFoundException();
            }

            List<UserRoleRelation> relations = userRoleRelationRepository.findByUserId(userId);

            List<Role> userRoleList = relations.stream().map(r -> r.getRole().getRole()).toList();

            return new UserRoleResponse(userRoleList);
        });
    }

    //Redis?


}
