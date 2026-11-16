package model.dto.request.user;

import lombok.*;
import model.dto.BaseData;
import model.dto.UserData;
import model.dto.request.UpdateRequest;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserRequest extends UpdateRequest {
    private String name;

    private String email;

    private String address;

    private String postal;

    private String phone;
}
