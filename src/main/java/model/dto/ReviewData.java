package model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class ReviewData {
    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request extends BaseData {
        private Long fundingPK;

        private String text;

        private BigDecimal rating;

    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Response extends BaseData {
        private Long pk;

        private Long fundingFK;

        private Long userFK;

        private String text;

        private BigDecimal rating;

        private LocalDateTime createdAt;

        private LocalDateTime updatedAt;

        private Long likeCount;

        private Long dislikeCount;

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
    public static class EditReview extends BaseData {

        private String text;

        private BigDecimal rating;
    }
}
