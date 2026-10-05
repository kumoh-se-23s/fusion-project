package model.domain;

import lombok.*;


@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class LocalAccount implements BaseDomain {
    @NonNull
    private Long pk;

    @NonNull
    private AppUser user;

    @NonNull
    private String id;

    @NonNull
    private String password;

    @NonNull
    private String salt;
}
