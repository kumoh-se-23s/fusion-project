package model.dto;

import constant.FundingState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class FundingData extends BaseData {
    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FundingDataRequest extends BaseData {
        private Long makerPK;

        private LocalDateTime startDate;

        private LocalDateTime endDate;

        private BigDecimal purposeAmount;

        private BigDecimal donateAmount;

        private String text;

        private byte[] imgFile;

        private List<Long> categoryPKs;

    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FundingDataResponse extends BaseData {
        private Long pk;

        private Long makerPK;

        private Long likeCount;

        private Long dislikeCount;

        private LocalDateTime startDate;

        private LocalDateTime endDate;

        private BigDecimal purposeAmount;

        private BigDecimal donateAmount;

        private FundingState state;

        private String text;

        private byte[] imgFile;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AdminCreateFundingDataRequest extends BaseData {

        private FundingDataRequest fundingDataRequest;

        private FundingState state;

    }
    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PKResponse extends BaseData {

        private Long fundingPK;

    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class EditFundingDataRequest extends BaseData {

        private String text;

        private Byte[] imgFile;

        private List<Long> categoryPKs;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AdminEditFundingData extends BaseData {
        private FundingDataRequest fundingDataRequest;

        private FundingState state;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ListFundingDataResponse extends BaseData {
        private List<FundingDataResponse> fundingDatumResponses;

        private Long amount;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AdminFundingDataResponse extends BaseData {
        private FundingDataResponse fundingDataResponse;

        private LocalDateTime createdAt;

        private LocalDateTime updatedAt;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AdminListFundingDataResponse extends BaseData {
        private List<AdminFundingDataResponse> adminFundingDatumResponses;

        private Long amount;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DeleteResponse extends BaseData {

        private Boolean isDeleted;

    }
}
