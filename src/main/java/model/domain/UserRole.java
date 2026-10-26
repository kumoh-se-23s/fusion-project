package model.domain;

import annotation.Column;
import annotation.PK;
import annotation.Table;
import lombok.*;

@Table(name = "USER_ROLE")
@Builder(toBuilder = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserRole extends BaseDomain {
    @PK
    @Column(name = "role_pk")
    private Long pk;

    @NonNull
    @Column(name = "role_name")
    private String roleName;
}
