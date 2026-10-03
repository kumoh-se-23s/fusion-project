package Domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserRoleRelation implements BaseDomain {

    private Long userRolePk;
    private UserRole role;
    private AppUser user;
}
