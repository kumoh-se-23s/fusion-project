package Domain;

import lombok.*;


@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class LocalAccount implements BaseDomain {

    private Long pk;
    private AppUser user;
    private String id;
    private String password;
    private String salt;
}
