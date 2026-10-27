package model.domain;

import annotation.Column;
import annotation.JoinColumn;
import repository.manager.Lazy;
import annotation.PK;
import annotation.Table;
import lombok.*;

@Table(name = "USER_ROLE_RELATION")
@Builder(toBuilder = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserRoleRelation extends BaseDomain {
    @PK
    @Column(name = "user_role_pk")
    private Long pk;

    @NonNull
    @JoinColumn(name = "role_fk", nullable = false, uniqueGroup = "user_role")
    private Lazy<UserRole> role;

    @NonNull
    @JoinColumn(name = "user_fk", nullable = false, uniqueGroup = "user_role")
    private Lazy<AppUser> user;
}
