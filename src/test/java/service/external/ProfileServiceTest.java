package service.external;

import example.db.DatabaseTest;
import model.domain.AppUser;
import model.dto.request.user.UpdateUserRequest;
import model.dto.response.IdResponse;
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
        profileService = new ProfileService(db.config.getUserRepository());
    }

    @Nested
    public class UpdateUser {

        @Test
        @DisplayName("유저 정보를 업데이트한다.")
        void success() {
            AppUser maker = db.maker1;
            long makerId = maker.getId();

            UpdateUserRequest request = UpdateUserRequest.builder()
                    .name("호날두")
                    .build();

            IdResponse response = profileService.updateUser(makerId, request);
            AppUser result = db.config.getUserRepository().findById(response.getId())
                    .orElseThrow(AssertionError::new);
            assertEquals(makerId, response.getId());
            assertEquals(result.getId(), response.getId());
            assertEquals("호날두", result.getName());
        }
    }
}
