package model.domain;

import annotation.Column;
import annotation.JoinColumn;
import annotation.PK;
import annotation.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Table(name = "FUNDING_CATEGORY")
@SuperBuilder(toBuilder = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FundingCategory extends BaseDomain {
    @PK
    @Column(name = "funding_category_pk")
    private Long pk;

    @JoinColumn(name = "category_fk", nullable = false, uniqueGroup = "funding_category")
    private Category category;

    @JoinColumn(name = "funding_fk", nullable = false, uniqueGroup = "funding_category")
    private Funding funding;
}
