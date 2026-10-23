package model.domain;

import annotation.Column;
import annotation.PK;
import annotation.Table;
import lombok.*;
import java.time.LocalDateTime;

@Table(name = "APP_USERS")
@Builder(toBuilder = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AppUser extends BaseDomain {
    @NonNull
    @PK
    @Column(name = "user_pk")
    private Long pk;

    @NonNull
    @Column(name = "user_name")
    private String name;

    @NonNull
    @Column(name = "user_email")
    private String email;

    @Column(name = "user_postal")
    private String postal;

    @Column(name = "user_address")
    private String address;

    @Column(name = "user_phone")
    private String phone;

    @NonNull
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
