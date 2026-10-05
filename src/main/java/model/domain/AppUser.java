package model.domain;

import lombok.*;

import java.time.LocalDateTime;

@Builder(toBuilder = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AppUser implements BaseDomain {
    @NonNull
    private Long pk;

    @NonNull
    private String name;

    @NonNull
    private String email;

    private String postal;
    private String address;
    private String phone;

    @NonNull
    private LocalDateTime createdAt;
}
