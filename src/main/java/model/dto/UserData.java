package model.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserData extends BaseData {
    private String name;

    private String email;

    private String address;

    private String postalCode;

    private String phone;
}