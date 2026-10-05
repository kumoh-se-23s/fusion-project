package DTO.Account;

import Annotation.DomainField;
import Annotation.NotNull;
import DTO.BaseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
