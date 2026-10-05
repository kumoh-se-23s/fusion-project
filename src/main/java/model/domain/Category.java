package model.domain;

import lombok.*;

@Builder(toBuilder = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Category implements BaseDomain {

    private Long pk;
    private String name;
    private String description;
}
