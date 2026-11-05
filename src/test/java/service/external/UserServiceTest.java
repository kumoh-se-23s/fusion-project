package service.external;

import example.db.DatabaseTest;
import model.Serializer;
import model.domain.AppUser;
import model.dto.UserData;
import model.dto.request.user.UpdateUserRequest;
import model.dto.response.IdResponse;
import model.dto.response.user.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import service.UserService;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("UserService")
public class UserServiceTest extends DatabaseTest {
    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(
                db.config.getUserRepository(),
                db.config.getUserRoleRepository(),
                db.config.getUserRoleRelationRepository()
        );
    }

    @Nested
    public class UpdateUser {

        @Test
        @DisplayName("유저 정보를 업데이트한다.")
        void success() {
            AppUser maker = db.maker1;
            long makerId = maker.getId();


            UserResponse userResponse = userService.getMyInfo(makerId);
            UserData testUserData = userResponse.getUserData();

            testUserData.setName("호날두");


            IdResponse response = userService.updateUser(makerId, new UpdateUserRequest(testUserData));
            AppUser result = db.config.getUserRepository().findById(response.getId())
                    .orElseThrow(AssertionError::new);
            assertEquals(makerId, response.getId());
            assertEquals(result.getId(), response.getId());
            assertEquals("호날두", result.getName());
        }
    }
}
