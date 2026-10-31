package model.domain;

import annotation.Column;
import annotation.JoinColumn;
import annotation.PK;
import annotation.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Table(name = "LOCAL_ACCOUNT")
@SuperBuilder(toBuilder = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LocalAccount extends BaseDomain {
    @PK
    @Column(name = "local_account_pk")
    private Long pk;

    @JoinColumn(name = "user_fk", nullable = false, unique = true)
    private AppUser user;

    @Column(name = "local_account_id", nullable = false, unique = true)
    private String id;

    @Column(name = "local_account_password", nullable = false)
    private String password;

    @Column(name = "local_account_salt", nullable = false)
    private String salt;
}
