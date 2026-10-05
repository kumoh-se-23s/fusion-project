package model.domain;

import lombok.*;


@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class UserRole implements BaseDomain {
    @NonNull
    private Long pk;

    @NonNull
    private String roleName;
}
