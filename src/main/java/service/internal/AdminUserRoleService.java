package service.internal;

import exception.AlreadyExistException;
import exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import model.domain.AppUser;
import model.domain.UserRole;
import model.domain.UserRoleRelation;
import model.dto.request.user_role.AssignRoleRequest;
import model.dto.response.DeleteResponse;
import model.dto.response.IdResponse;
import model.dto.response.UserRole.UserRoleResponse;
import repository.UserRepository;
import repository.UserRoleRelationRepository;
import repository.UserRoleRepository;
import service.BaseService;
import util.RedisUtil;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class AdminUserRoleService extends BaseService {
    //Redis에 저장하는 사용자별 역할 명단의 키 접두사 (예: roles:7)
    private static final String ROLE_KEY_PREFIX = "roles:";

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserRoleRelationRepository userRoleRelationRepository;
    private final RedisUtil redisUtil;

    //사용자에게 역할 부여
    public IdResponse assignRole(AssignRoleRequest request) {
        Long relationId = tx.execute(() -> {
            AppUser user = userRepository.findById(request.getUserId())
                    .orElseThrow(NotFoundException::new);

            UserRole role = userRoleRepository.findByName(request.getRole()).orElseThrow(NotFoundException::new);

            if (userRoleRelationRepository.findByUserAndRole(request.getUserId(), role.getId()).isPresent()) {
                throw new AlreadyExistException();
            }

            UserRoleRelation relation = UserRoleRelation.builder()
                    .user(user)
                    .role(role)
                    .build();

            return userRoleRelationRepository.create(relation).getId();
        });

        //커밋이 끝난 뒤 Redis 명단을 지운다. 다음 요청 때 MySQL 기준으로 다시 채워진다
        redisUtil.delete(ROLE_KEY_PREFIX + request.getUserId());

        return new IdResponse(relationId);
    }

    public DeleteResponse removeRole(long userId) {
        UserRoleRelation userRoleRelation = userRoleRelationRepository.findById(userId).orElseThrow(NotFoundException::new);

        boolean result = userRoleRelationRepository.delete(userRoleRelation.getId());

        return new DeleteResponse(result);
    }

    public UserRoleResponse getRoleByUser(long userId) {
        List<UserRoleRelation> userRoleRelation = userRoleRelationRepository.findByUserId(userId);

        List<String> reusltList = new ArrayList<>();

        userRoleRelation.stream().forEach((e) -> {
            UserRole userRole = e.getRole();
            reusltList.add(userRole.getRoleName());
        });

        return new UserRoleResponse(reusltList);
    }
}
