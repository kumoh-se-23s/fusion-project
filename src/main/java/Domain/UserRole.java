package Domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// ============================================================
// USER_ROLE
// ============================================================
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserRole {

    private Long rolePk;
    private String roleName;
}
