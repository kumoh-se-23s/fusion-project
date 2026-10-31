package service.external;

import example.db.DatabaseTest;
import model.Serializer;
import model.domain.AppUser;
import model.dto.UserData;
import model.dto.request.user.UpdateUserRequest;
import model.dto.response.PKResponse;
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
            long makerPK = maker.getPk();


            UserResponse userResponse = userService.getMyInfo(makerPK);
            UserData testUserData = userResponse.getUserData();

            testUserData.setName("호날두");


            PKResponse response = userService.updateUser(makerPK, new UpdateUserRequest(testUserData));
            AppUser result = db.config.getUserRepository().findByPK(response.getPk())
                    .orElseThrow(AssertionError::new);
            assertEquals(makerPK, response.getPk());
            assertEquals(result.getPk(), response.getPk());
            assertEquals("호날두", result.getName());
        }
    }
}
