package model.dto.request.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import model.dto.AccountData;
import model.dto.BaseData;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest extends BaseData {
    private AccountData accountData;
}
