package DTO.User;

import Annotation.DomainField;
import DTO.BaseDTO;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SignupRequestData extends BaseDTO {

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

    @DomainField("createdAt")
    private LocalDateTime createdAt;





}
