package DTO.User;

import Annotation.DomainField;
import Annotation.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDTO {

    @NotNull
    @DomainField("localAccountPK")
    private String localAccountPK;

    @NotNull
    @DomainField("name")
    private String name;

    @NotNull
    @DomainField("email")
    private String email;

    @NotNull
    @DomainField("postalCode")
    private String postalCode;

    @NotNull
    @DomainField("address")
    private String address;

    @NotNull
    @DomainField("phone")
    private String phone;

}
