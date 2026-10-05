package model.domain;

import lombok.*;

@Builder(toBuilder = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DenyFunding implements BaseDomain {
    private Long pk;
    private Funding funding;
    private String reason;
}
