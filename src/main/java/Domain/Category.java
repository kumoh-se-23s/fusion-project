package Domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Category implements BaseDomain {

    private Long categoryPk;
    private String categoryName;
    private String categoryDescription;
}
