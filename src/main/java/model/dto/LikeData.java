package model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class LikeData {
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    private static class request {
        private Boolean likeType;
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
}
