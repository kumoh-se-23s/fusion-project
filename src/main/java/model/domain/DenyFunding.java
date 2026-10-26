package model.domain;

import annotation.Column;
import annotation.JoinColumn;
import annotation.PK;
import annotation.Table;
import lombok.*;

@Table(name = "DENY_FUNDING")
@Builder(toBuilder = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DenyFunding extends BaseDomain {
    @PK
    @Column(name = "deny_funding_pk")
    private Long pk;

    @NonNull
    @JoinColumn(name = "funding_fk")
    private Funding funding;

    @NonNull
    @Column(name = "deny_reason")
    private String reason;
}
