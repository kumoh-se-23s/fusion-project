package service.internal;

import constant.Role;
import exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import model.domain.UserRole;
import model.domain.UserRoleRelation;
import model.dto.response.DeleteResponse;
import repository.UserRepository;
import repository.UserRoleRelationRepository;
import repository.UserRoleRepository;
import service.BaseService;
import util.RedisUtil;

@RequiredArgsConstructor
public class AdminUserService extends BaseService {
    private final UserRepository userRepository;
    private final RedisUtil redisUtil;

    public editUser() {

    }



    public DeleteResponse deleteUser(long userId) {
        DeleteResponse response = tx.execute(() -> {
            if (!userRepository.isExistById(userId)) {
                throw new NotFoundException();
            }

            boolean result = userRepository.delete(userId);
            return new DeleteResponse(result);
        });

        redisUtil.delete("access" + userId + " : ");
        redisUtil.delete("refresh" + userId + " : ");

        return response;
    }
}
