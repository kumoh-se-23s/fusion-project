package service.external;

import example.db.DatabaseTest;
import model.Serializer;
import model.domain.AppUser;
import model.dto.UserData;
import model.dto.request.user.UpdateUserRequest;
import model.dto.response.PKResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import service.UserService;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
    class UpdateUser {

        @Test
        @DisplayName("유저 정보를 업데이트한다.")
        void success() {
            AppUser maker1 = db.maker1;
            long maker1PK = maker1.getPk();

            AppUser maker2 = db.maker2;
            long maker2PK = maker2.getPk();

            Map<Long, AppUser> dataMap = new HashMap<Long, AppUser>();
            dataMap.put(maker1PK, maker1);
            dataMap.put(maker2PK, maker2);

            for(Map.Entry<Long, AppUser> entry : dataMap.entrySet()) {
                Long key = entry.getKey();
                AppUser appUser = entry.getValue();

                UserData userData = Serializer.toData(appUser, UserData.class);

                PKResponse response = userService.updateUser(key, new UpdateUserRequest(userData));
                assertEquals(key, response.getPk());
                System.out.println(response.getPk());
            }
        }
    }
}
