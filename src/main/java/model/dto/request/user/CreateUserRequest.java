package model.dto.request.user;

import lombok.*;
import model.dto.AccountData;
import model.dto.UserData;
import model.dto.request.Request;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateUserRequest extends Request {
    private UserData userData;
}
