package DTO.User;

import Annotation.DomainField;
import DTO.BaseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SignupResponseData extends BaseDTO {

    @DomainField("access_token")
    private String accessToken;

    @DomainField("refresh_token")
    private String refreshToken;

}
