package model.dto.request.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import model.dto.request.Request;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateSocialUserRequest extends Request {
    private String address;

    private String postal;

    private String phone;

}
