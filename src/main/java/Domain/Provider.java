package Domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// ============================================================
// PROVIDER
// ============================================================
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Provider {

    private Long socialProviderPk;
    private String socialName;
}
