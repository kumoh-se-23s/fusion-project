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
import repository.Filter;
import repository.UserRepository;
import repository.UserRoleRelationRepository;
import repository.UserRoleRepository;
import service.BaseService;


import java.util.List;
import java.util.Map;

public class ProfileService extends BaseService {
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserRoleRelationRepository userRoleRelationRepository;

    public ProfileService(UserRepository userRepository, UserRoleRepository userRoleRepository, UserRoleRelationRepository userRoleRelationRepository) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.userRoleRelationRepository = userRoleRelationRepository;
    }

    public UserResponse getMyInfo(long userId) {
        AppUser appUser = userRepository.findById(userId).orElseThrow(NotFoundException::new);

        UserData userData = Serializer.toData(appUser, UserData.class);

        return new UserResponse(userData);

    }

    public IdResponse updateUser(long userId, UpdateUserRequest request) {
        UserData userData = request.getUserData();

        Long updateResult = userRepository.updateUser(userId, userData).orElseThrow(NotFoundException::new);

        return new IdResponse(updateResult);
    }

    //키값 검증하는 로직 필요
    public DeleteResponse deleteUser(long userId) {
        return new DeleteResponse(userRepository.delete(userId));

    }

    //List로 만들어야 함
    public List<UserRoleRelation> getRoles(long userId) {
        AppUser appUser = userRepository.findById(userId).orElseThrow(NotFoundException::new);

        return userRoleRelationRepository.find(Filter.builder()
                .conditions(Map.of("user", appUser))
                .build()
        );
    }
}
