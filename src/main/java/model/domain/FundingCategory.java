package model.domain;

import annotation.Column;
import annotation.JoinColumn;
import annotation.PK;
import annotation.Table;
import lombok.*;

@Table(name = "FUNDING_CATEGORY")
@Builder(toBuilder = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FundingCategory extends BaseDomain {
    @PK
    @Column(name = "funding_category_pk")
    private Long pk;

    @NonNull
    @JoinColumn(name = "category_fk")
    private Category category;

    @NonNull
    @JoinColumn(name = "funding_fk")
    private Funding funding;
}
