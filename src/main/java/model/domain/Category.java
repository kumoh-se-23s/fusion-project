package model.domain;

import lombok.*;

@Builder(toBuilder = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Category implements BaseDomain {
    @NonNull
    private Long pk;

    @NonNull
    private String name;

    private String description;
}
