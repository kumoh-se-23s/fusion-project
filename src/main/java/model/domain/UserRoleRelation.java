package model.domain;

import annotation.Column;
import annotation.JoinColumn;
import annotation.PK;
import annotation.Table;
import lombok.*;

@Table(name = "USER_ROLE_RELATION")
@Builder(toBuilder = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserRoleRelation extends BaseDomain {
    @NonNull
    @PK
    @Column(name = "user_role_pk")
    private Long pk;

    @NonNull
    @JoinColumn(name = "role_fk")
    private UserRole role;

    @NonNull
    @JoinColumn(name = "user_fk")
    private AppUser user;
}
