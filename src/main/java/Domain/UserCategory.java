package Domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// ============================================================
// USER_CATEGORY
// ============================================================
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserCategory {

    private Long userCategoryPk;
    private AppUser user;
    private Category category;
}
