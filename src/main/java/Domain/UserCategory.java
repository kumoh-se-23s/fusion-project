package Domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserCategory implements BaseDomain {

    private Long userCategoryPk;
    private AppUser user;
    private Category category;
}
