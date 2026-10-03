package Domain;

import Domain.Enum.SocialProvider;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Provider implements BaseDomain {

    private Long socialProviderPk;
    private SocialProvider socialName;
}
