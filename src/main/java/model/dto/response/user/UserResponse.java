package model.dto.response.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import model.dto.BaseData;
import model.dto.UserData;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse extends BaseData {
    private UserData userData;
}
