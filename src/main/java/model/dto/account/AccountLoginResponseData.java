package model.dto.account;

import Annotation.DomainField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import model.dto.BaseDTO;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccountLoginResponseData extends BaseDTO {

    @DomainField("access_token")
    private String accessToken;

    @DomainField("refresh_token")
    private String refreshToken;
}
