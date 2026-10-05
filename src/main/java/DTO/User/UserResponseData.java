package DTO.User;

import Annotation.DomainField;
import Annotation.NotNull;
import DTO.BaseDTO;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseData extends BaseDTO {

    @DomainField("localAccountPK")
    private String localAccountPK;

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

    @DomainField("created_at")
    private String createdAt;

}
