package DTO.User;

import Annotation.DomainField;
import Annotation.NotNull;
import DTO.BaseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LocalSignupRequestDTO extends BaseDTO {

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

    @DomainField("phone")
    private String phone;

    @NotNull
    @DomainField("createdAt")
    private LocalDateTime createdAt;





}
