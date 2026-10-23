package model.dto.request.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import model.dto.AccountData;
import model.dto.UserData;
import model.dto.request.Request;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SignUpRequest extends Request {
    private AccountData accountData;

    private UserData userData;
}
