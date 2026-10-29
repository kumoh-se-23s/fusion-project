package service;

import model.domain.AppUser;
import model.dto.UserData;
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
}
