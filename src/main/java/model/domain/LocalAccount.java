package model.domain;

import annotation.Column;
import annotation.JoinColumn;
import annotation.PK;
import annotation.Table;
import lombok.*;

@Table(name = "LOCAL_ACCOUNT")
@Builder(toBuilder = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LocalAccount extends BaseDomain {
    @PK
    @Column(name = "local_account_pk")
    private Long pk;

    @NonNull
    @JoinColumn(name = "user_fk")
    private AppUser user;

    @NonNull
    @Column(name = "local_account_id")
    private String id;

    @NonNull
    @Column(name = "local_account_password")
    private String password;

    @NonNull
    @Column(name = "local_account_salt")
    private String salt;
}
