package model.domain;

import annotation.Column;
import annotation.JoinColumn;
import annotation.OnDelete;
import annotation.PK;
import annotation.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Table(name = "DENY_FUNDING")
@SuperBuilder(toBuilder = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DenyFunding extends BaseDomain {
    @PK
    @Column(name = "deny_funding_pk")
    private Long pk;

    @JoinColumn(name = "funding_fk", nullable = false, onDelete = OnDelete.CASCADE)
    private Funding funding;

    @Column(name = "deny_reason", nullable = false, length = 500)
    private String reason;
}
