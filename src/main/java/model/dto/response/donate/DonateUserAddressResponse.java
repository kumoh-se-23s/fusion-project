package model.dto.response.donate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import model.dto.response.Response;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DonateUserAddressResponse extends Response {
    private Long donatePK;

    private Long userPK;

    private String address;

}
