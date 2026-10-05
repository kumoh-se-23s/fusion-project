package DTO.User;

import Annotation.DomainField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminUserUpdateRequestData {

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
}
