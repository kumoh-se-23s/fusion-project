package model.dto.response.donate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import model.domain.Donate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DonateUserAddressResponse {
    private Long donatePK;

    private Long userPK;

    private String address;

}
