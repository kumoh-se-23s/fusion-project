package model.domain;

import annotation.Column;
import annotation.PK;
import annotation.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Table(name = "USER_ROLE")
@SuperBuilder(toBuilder = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserRole extends BaseDomain {
    @PK
    @Column(name = "role_pk")
    private Long pk;

    @NonNull
    @Column(name = "role_name", nullable = false, unique = true, length = 50)
    private String roleName;
}
