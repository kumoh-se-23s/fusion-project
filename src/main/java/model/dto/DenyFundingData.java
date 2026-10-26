package model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DenyFundingData extends BaseData {
    private Long pk;

    private Long categoryPK;

    private String denyReason;
}
