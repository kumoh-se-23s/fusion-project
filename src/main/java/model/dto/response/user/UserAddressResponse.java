package model.dto.response.user;

import annotation.BindDomain;
import annotation.DataField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import model.domain.AppUser;
import model.dto.response.Response;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@BindDomain(values = AppUser.class)
public class UserAddressResponse extends Response {
    @DataField(name = "pk")
    private Long userPK;

    private String address;

}
