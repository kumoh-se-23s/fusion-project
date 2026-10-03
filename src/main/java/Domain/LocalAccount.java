package Domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LocalAccount implements BaseDomain {

    private Long localAccountPk;
    private AppUser user;
    private String localAccountId;
    private String localAccountPassword;
    private String localAccountSalt;
}
