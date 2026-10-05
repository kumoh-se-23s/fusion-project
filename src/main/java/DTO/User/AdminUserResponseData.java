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
public class AdminUserResponseData extends BaseDTO {

    @DomainField("user_pk")
    private String userPK;

    @DomainField("account_pk")
    private String accountPK;

    @DomainField("id")
    private String id;

    @DomainField("name")
    private String name;

    @DomainField("email")
    private String email;

    @DomainField("address")
    private String address;

    @DomainField("postal_code")
    private String postalCode;

    @DomainField("phone")
    private String phone;

    @DomainField("created_at")
    private String createdAt;
}
