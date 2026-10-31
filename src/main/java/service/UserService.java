package service;

import exception.NotFoundException;
import model.Serializer;
import model.domain.AppUser;
import model.domain.UserRole;
import model.domain.UserRoleRelation;
import model.dto.UserData;
import model.dto.request.user.UpdateUserRequest;
import model.dto.response.DeleteResponse;
import model.dto.response.PKResponse;
import model.dto.response.user.UserResponse;
import repository.Filter;
import repository.UserRepository;
import repository.UserRoleRelationRepository;
import repository.UserRoleRepository;


import java.util.List;
import java.util.Map;
import java.util.Optional;

public class UserService extends BaseService {
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserRoleRelationRepository userRoleRelationRepository;

    public UserService(UserRepository userRepository, UserRoleRepository userRoleRepository, UserRoleRelationRepository userRoleRelationRepository) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.userRoleRelationRepository = userRoleRelationRepository;
    }

    public UserResponse getMyInfo(long userPk) {
        AppUser appUser = userRepository.findByPK(userPk).orElseThrow(NotFoundException::new);

        UserData userData = Serializer.toData(appUser, UserData.class);

        return new UserResponse(userData);

//        if (optionalAppUser.isPresent()) {
//            AppUser appUser = optionalAppUser.get();
//
//            UserData userData = Serializer.toData(appUser, UserData.class);
//
//            return new UserResponse(userData);
//        } else {
//            throw new NotFoundException();
//        }
    }

    public PKResponse updateUser(long userPk, UpdateUserRequest request) {
        UserData userData = request.getUserData();

        Long updateResult = userRepository.updateUser(userPk, userData).orElseThrow(NotFoundException::new);

        return new PKResponse(updateResult);
    }

    //키값 검증하는 로직 필요
    public DeleteResponse deleteUser(long userPk) {
        return new DeleteResponse(userRepository.delete(userPk));

    }

    //List로 만들어야 함
    public List<UserRoleRelation> getRoles(long userPk) {
        AppUser appUser = userRepository.findByPK(userPk).orElseThrow(NotFoundException::new);

        return userRoleRelationRepository.find(Filter.builder()
                .conditions(Map.of("user_fk", appUser))
                .build()
        );
    }
}
