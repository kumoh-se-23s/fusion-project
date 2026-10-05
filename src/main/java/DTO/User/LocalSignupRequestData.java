package DTO.User;

import Annotation.DomainField;
import DTO.BaseDTO;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LocalSignupRequestData extends BaseDTO {

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

    @DomainField("phone")
    private String phone;

    @NonNull
    @DomainField("createdAt")
    private LocalDateTime createdAt;





}
