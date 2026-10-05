package model.domain;

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
