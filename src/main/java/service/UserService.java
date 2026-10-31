package service;

import exception.NotFoundException;
import model.domain.AppUser;
import model.domain.UserRole;
import model.domain.UserRoleRelation;
import model.dto.UserData;
import model.dto.request.user.UpdateUserRequest;
import model.dto.response.DeleteResponse;
import model.dto.response.PKResponse;
import model.dto.response.user.UserResponse;
import repository.Filter;
import repository.UserRoleRelationRepository;
import repository.UserRoleRepository;
import repository.user.UserRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class UserService extends BaseService {
    private UserRepository userRepository;
    private UserRoleRepository userRoleRepository;
    private UserRoleRelationRepository userRoleRelationRepository;

    public UserResponse getMyInfo(long userPk) {
        Optional<AppUser> optionalAppUser = userRepository.findUser(userPk);

        if (optionalAppUser.isPresent()) {
            AppUser appUser = optionalAppUser.get();

            UserData userData = UserData.builder()
                    .name(appUser.getName())
                    .email(appUser.getEmail())
                    .address(appUser.getAddress())
                    .postalCode(appUser.getPostal())
                    .phone(appUser.getPhone())
                    .build();

            return new UserResponse(userData);
        } else {
            throw new NotFoundException();
        }
    }

    public PKResponse updateUser(long userPk, UpdateUserRequest request) {
        Optional<AppUser> optionalAppUser = userRepository.findAppUserByUserPk(userPk);

        if (optionalAppUser.isPresent()) {
            UserData userData = request.getUserData();

            Optional<Long> updateResult = userRepository.updateUser(userPk, userData);

            if (updateResult.isPresent()) {
                return new PKResponse(updateResult.get());
            } else {
                throw new NotFoundException();
            }
        } else {
            throw new NotFoundException();
        }
    }

    //키값 검증하는 로직 필요
    public DeleteResponse deleteUser(long userPk) {
        return new DeleteResponse(userRepository.delete(userPk));

    }

    //List로 만들어야 함
    public List<UserRoleRelation> getRoles(long userPk) {
        return userRoleRelationRepository.find(Filter.builder()
                .conditions(Map.of("pk", userPk))
                .limit(1)
                .build()
        );
    }
}
