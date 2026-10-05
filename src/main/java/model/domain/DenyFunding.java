package model.domain;

import lombok.*;

@Builder(toBuilder = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DenyFunding implements BaseDomain {
    @NonNull
    private Long pk;

    @NonNull
    private Funding funding;

    @NonNull
    private String reason;
}
