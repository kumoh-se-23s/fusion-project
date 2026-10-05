package model.dto.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import model.dto.BaseDTO;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccountChangePasswordResponseData extends BaseDTO {

    @Annotation.DomainField("user_pk")
    private String user_pk;
}
