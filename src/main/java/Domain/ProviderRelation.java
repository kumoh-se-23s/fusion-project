package Domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProviderRelation implements BaseDomain {

    private Long accountProviderPk;
    private SocialAccount socialAccount;
    private Provider socialProvider;
}
