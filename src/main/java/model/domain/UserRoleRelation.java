package model.domain;

import lombok.*;


@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class UserRoleRelation implements BaseDomain {
    @NonNull
    private Long pk;

    @NonNull
    private UserRole role;

    @NonNull
    private AppUser user;
}
