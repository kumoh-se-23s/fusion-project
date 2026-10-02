package Domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// ============================================================
// CATEGORY
// ============================================================
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    private Long categoryPk;
    private String categoryName;
    private String categoryDescription;
}
