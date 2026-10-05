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
    public static class Request extends BaseData {
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
    public static class Response extends BaseData {
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
    public static class ListResponse extends BaseData {
        private List<Response> ListResponses;

        private Long amount;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PKResponse extends BaseData {

        private Long PK;

    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DeleteResponse extends BaseData {

        private Boolean isDeleted;

    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AdminCreateRequest extends BaseData {

        private Request request;

        private FundingState state;

    }


    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class EditRequest extends BaseData {

        private String text;

        private Byte[] imgFile;

        private List<Long> categoryPKs;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AdminEditRequest extends BaseData {
        private Request request;

        private FundingState state;
    }



    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AdminResponse extends BaseData {
        private Response response;

        private LocalDateTime createdAt;

        private LocalDateTime updatedAt;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AdminListResponse extends BaseData {
        private List<AdminResponse> adminResponses;

        private Long amount;
    }


}
