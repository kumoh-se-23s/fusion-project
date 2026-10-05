package model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class DenyFundingData {
    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DenyFundingDataRequest extends BaseData {
        private String denyReason;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DenyFundingDataResponse extends BaseData {
        private Long pk;

        private Long categoryPK;

        private String denyReason;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ListDenyFundingDataResponse extends BaseData {
        private List<CategoryData.CategoryDataResponse> fundingDatumResponses;

        private Long amount;
    }

}
