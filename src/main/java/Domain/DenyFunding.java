package Domain;

import lombok.*;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DenyFunding implements BaseDomain {
    private Long pk;
    private Funding funding;
    private String reason;
}
