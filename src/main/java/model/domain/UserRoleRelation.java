package model.domain;

import annotation.Column;
import annotation.JoinColumn;
import annotation.OnDelete;
import annotation.PK;
import annotation.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Table(name = "USER_ROLE_RELATION")
@SuperBuilder(toBuilder = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserRoleRelation extends BaseDomain {
    @PK
    @Column(name = "user_role_pk")
    private Long pk;

    @JoinColumn(name = "role_fk", nullable = false, uniqueGroup = "user_role", onDelete = OnDelete.CASCADE)
    private UserRole role;

    @JoinColumn(name = "user_fk", nullable = false, uniqueGroup = "user_role", onDelete = OnDelete.CASCADE)
    private AppUser user;
}
