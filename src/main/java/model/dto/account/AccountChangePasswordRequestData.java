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
public class AccountChangePasswordRequestData extends BaseDTO {

    @DomainField("current_password")
    private String currentPassword;

    @DomainField("new_password")
    private String newPassword;
}
