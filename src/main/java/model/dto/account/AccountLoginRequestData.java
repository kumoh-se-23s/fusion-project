package model.dto.account;

import Annotation.DomainField;
import Annotation.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import model.dto.BaseDTO;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccountLoginRequestData extends BaseDTO {

    @DomainField("id")
    private String id;

    @DomainField("password")
    private String password;

}
