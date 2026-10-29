package service;

import model.domain.AppUser;
import model.dto.UserData;
import model.dto.request.user.UpdateUserRequest;
import model.dto.response.PKResponse;
import model.dto.response.user.UserResponse;
import repository.user.UserRepository;

import java.util.Optional;

public class UserService {
    private UserRepository userRepository;

    public UserResponse getMyInfo(long userPk) {
        try {
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

            }
        } catch () {

        }
    }

    public PKResponse updateUser(long userPk, UpdateUserRequest request) {
        try {
            Optional<AppUser> optionalAppUser = userRepository.findByUserPk(userPk);

            if(optionalAppUser.isPresent()) {
                UserData userData = request.getUserData();

                Optional<Long> updateResult = userRepository.updateUser(userPk, userData);

                if(updateResult.isPresent()) {
                    return new PKResponse(updateResult.get());
                } else {
                    throw
                }
            } else {
                throw
            }

        } catch ()
    }
}
