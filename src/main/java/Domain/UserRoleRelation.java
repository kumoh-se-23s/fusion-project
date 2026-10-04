package Domain;

import lombok.*;


@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class UserRoleRelation implements BaseDomain {

    private Long pk;
    private UserRole role;
    private AppUser user;
}
