package model.dto;

import annotation.BindDomain;
import annotation.DataField;
import lombok.*;
import model.domain.AppUser;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@BindDomain(values = AppUser.class)
public class UserData extends BaseData {
    private String name;

    private String email;

    private String address;

    private String postal;

    private String phone;
}