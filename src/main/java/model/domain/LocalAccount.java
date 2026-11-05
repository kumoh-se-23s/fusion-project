package model.domain;

import annotation.Column;
import annotation.JoinColumn;
import annotation.OnDelete;
import annotation.Id;
import annotation.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Table(name = "LOCAL_ACCOUNT")
@SuperBuilder(toBuilder = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LocalAccount extends BaseDomain {
    @Id
    @Column(name = "local_account_id")
    private Long id;

    @JoinColumn(name = "user_fk", nullable = false, unique = true, onDelete = OnDelete.CASCADE)
    private AppUser user;

    @Column(name = "local_account_login_id", nullable = false, unique = true)
    private String loginId;

    @Column(name = "local_account_password", nullable = false)
    private String password;

    @Column(name = "local_account_salt", nullable = false)
    private String salt;
}
