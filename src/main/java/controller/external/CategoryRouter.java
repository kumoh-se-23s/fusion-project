package controller.external;

import com.minihttp.core.route.BaseRouter;
import com.minihttp.core.route.annotation.*;
import com.minihttp.packet.HttpRequest;
import model.dto.CategoryData;
import model.dto.request.category.CategoryRequest;
import model.dto.response.DataCollectionResponse;
import model.dto.response.DeleteResponse;
import model.dto.response.IdResponse;
import model.dto.response.SingleDataResponse;
import service.external.CategoryService;
import service.internal.AdminCategoryService;

import java.util.Optional;


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
