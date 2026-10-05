package model.domain;

import lombok.*;

import java.time.LocalDateTime;

@Builder(toBuilder = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AppUser implements BaseDomain {

    private Long pk;
    private String name;
    private String email;
    private String postal;
    private String address;
    private String phone;
    private LocalDateTime createdAt;
}
