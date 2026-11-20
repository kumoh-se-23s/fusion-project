package service.external;

import example.db.DatabaseTest;
import model.domain.AppUser;
import model.dto.UserData;
import model.dto.request.user.UpdateUserRequest;
import model.dto.response.IdResponse;
import model.dto.response.user.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ProfileService")
public class ProfileServiceTest extends DatabaseTest {
    private ProfileService profileService;

    @BeforeEach
    void setUp() {
        profileService = new ProfileService(
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


            UserResponse userResponse = profileService.getMyInfo(makerId);
            UserData testUserData = userResponse.getUserData();

            testUserData.setName("호날두");


            IdResponse response = profileService.updateUser(makerId, new UpdateUserRequest(testUserData));
            AppUser result = db.config.getUserRepository().findById(response.getId())
                    .orElseThrow(AssertionError::new);
            assertEquals(makerId, response.getId());
            assertEquals(result.getId(), response.getId());
            assertEquals("호날두", result.getName());
        }
    }
}
