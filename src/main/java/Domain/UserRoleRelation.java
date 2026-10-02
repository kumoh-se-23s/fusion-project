package Domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// ============================================================
// USER_ROLE_RELATION
// ============================================================
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserRoleRelation {

    private Long userRolePk;
    private UserRole role;
    private AppUser user;
}
