package Domain;

import lombok.*;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Category implements BaseDomain {

    private Long pk;
    private String name;
    private String description;
}
