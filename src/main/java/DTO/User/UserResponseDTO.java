package DTO.User;

import Annotation.DomainField;
import Annotation.NotNull;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDTO {

    @NonNull
    @DomainField("localAccountPK")
    private String localAccountPK;

    @NonNull
    @DomainField("name")
    private String name;

    @NonNull
    @DomainField("email")
    private String email;

    @NonNull
    @DomainField("postalCode")
    private String postalCode;

    @NonNull
    @DomainField("address")
    private String address;

    @NonNull
    @DomainField("phone")
    private String phone;

}
