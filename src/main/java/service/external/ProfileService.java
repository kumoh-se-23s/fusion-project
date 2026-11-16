package service.external;

import exception.NotFoundException;
import model.Serializer;
import model.domain.AppUser;
import model.domain.UserRoleRelation;
import model.dto.UserData;
import model.dto.request.user.UpdateUserRequest;
import model.dto.response.DeleteResponse;
import model.dto.response.IdResponse;
import model.dto.response.user.UserResponse;
import repository.*;
import service.BaseService;


import java.util.List;
import java.util.Map;

public class ProfileService extends BaseService {
    private final UserRepository userRepository;


    public ProfileService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserResponse getMyInfo(long userId) {
        AppUser appUser = userRepository.findById(userId).orElseThrow(NotFoundException::new);

        UserData userData = Serializer.toData(appUser, UserData.class);

        return new UserResponse(userData);

    }

    public IdResponse updateUser(long userId, UpdateUserRequest request) {
        Long updateResult = userRepository.updateUser(userId, request.toUpdateMap()).orElseThrow(NotFoundException::new);

        return new IdResponse(updateResult);
    }
}
