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
public class UserUpdateRequestDTO {

    @DomainField("name")
    private String name;

    @DomainField("email")
    private String email;

    @DomainField("postalCode")
    private String postalCode;

    @DomainField("address")
    private String address;

    @DomainField("phone")
    private String phone;
}
