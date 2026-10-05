package DTO.Account;

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
public class AccountChangePasswordRequestData extends BaseDTO {

    @DomainField("current_password")
    private String currentPassword;

    @DomainField("new_password")
    private String newPassword;
}
