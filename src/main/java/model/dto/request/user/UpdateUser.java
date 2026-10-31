package model.dto.request.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import model.dto.AccountData;
import model.dto.BaseData;
import model.dto.UserData;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUser extends BaseData {
    private UserData userData;
}