package model.dto;

import lombok.*;
import java.util.List;

public class CategoryData extends BaseData {
    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request extends BaseData {

        private String name;

        private String description;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Response extends BaseData {
        private Long pk;

        private String name;

        private String description;
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
    public static class BindRequest extends BaseData {
        private Long fundingPKs;

        private List<Long> categoryPKs;
    }
    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BindResponse extends BaseData {

        private List<Long> fundingCategoryPKs;
    }
}

