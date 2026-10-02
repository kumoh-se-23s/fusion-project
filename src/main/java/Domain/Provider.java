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
public class Provider {

    private Long socialProviderPk;
    private SocialProvider socialName;
}
