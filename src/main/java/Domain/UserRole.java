package Domain;

import lombok.*;


@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class UserRole implements BaseDomain {

    private Long pk;
    private String roleName;
}
