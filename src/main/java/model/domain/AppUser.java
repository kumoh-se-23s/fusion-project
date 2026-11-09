package model.domain;

import annotation.Column;
import annotation.Id;
import annotation.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.time.LocalDateTime;

@Table(name = "APP_USERS")
@SuperBuilder(toBuilder = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AppUser extends BaseDomain {
    @Id
    @Column(name = "user_id")
    private Long id;

    //실명
    @Column(name = "user_name", nullable = false)
    private String name;

    //이름
    @Column(name = "user_nick_name", unique = true, nullable = false)
    private String nickName;

    @Column(name = "user_email", unique = true, nullable = false)
    private String email;

    @Column(name = "user_postal", length = 20)
    private String postal;

    @Column(name = "user_address")
    private String address;

    @Column(name = "user_phone", unique = true, length = 30)
    private String phone;

    @Builder.Default
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
