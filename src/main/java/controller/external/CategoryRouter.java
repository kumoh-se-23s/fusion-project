package controller.external;

import com.minihttp.core.route.BaseRouter;
import com.minihttp.core.route.annotation.*;
import model.dto.CategoryData;
import model.dto.response.DataCollectionResponse;
import service.external.CategoryService;


/**
 * 관리자 전용 카테고리 라우터
 * @author 김민규
 * @since 2026-11-14
 * @version 1.0
 */
public class CategoryRouter extends BaseRouter {
    private final CategoryService categoryService;

    public CategoryRouter(CategoryService categoryService) {
        super("/category");
        this.categoryService = categoryService;
    }

    @Get()
    public DataCollectionResponse<CategoryData> getAllCategory() {
        return categoryService.getAllCategory();
    }
}
