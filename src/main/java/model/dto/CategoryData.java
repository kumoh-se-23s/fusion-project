package model.dto;

import lombok.*;

import java.util.List;

public class CategoryData extends BaseData {
    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CategoryDataRequest extends BaseData {

        private String name;

        private String description;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CategoryDataResponse extends BaseData {
        private Long pk;

        private String name;

        private String description;
    }
    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PKResponse extends BaseData {

        private Long categoryPK;

    }
    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ListCategoryDataResponse extends BaseData {
        private List<CategoryDataResponse> fundingDatumResponses;

        private Long amount;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BindCategoryDataRequest extends BaseData {
        private Long fundingPKs;

        private List<Long> categoryPKs;
    }


}
