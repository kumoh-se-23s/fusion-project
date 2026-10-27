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
    @PK
    @Column(name = "user_pk")
    private Long pk;

    @NonNull
    @Column(name = "user_name", nullable = false)
    private String name;

    @NonNull
    @Column(name = "user_email", unique = true)
    private String email;

    @Column(name = "user_postal", length = 20)
    private String postal;

    @Column(name = "user_address")
    private String address;

    @Column(name = "user_phone", length = 30)
    private String phone;

    @Builder.Default
    @NonNull
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
