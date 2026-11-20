package controller.internal;

import com.minihttp.core.route.BaseRouter;
import com.minihttp.core.route.annotation.*;
import model.dto.CategoryData;
import model.dto.request.category.CategoryRequest;
import model.dto.response.DataCollectionResponse;
import model.dto.response.DeleteResponse;
import model.dto.response.IdResponse;
import model.dto.response.SingleDataResponse;
import service.internal.AdminCategoryService;


/**
 * 관리자 전용 카테고리 라우터
 * @author 김민규
 * @since 2026-11-14
 * @version 1.0
 */
public class AdminCategoryRouter extends BaseRouter {
    private final AdminCategoryService adminCategoryService;

    public AdminCategoryRouter(AdminCategoryService adminCategoryService) {
        super("/internal/category");
        this.adminCategoryService = adminCategoryService;
    }

    @Post()
    public IdResponse createCategory(CategoryRequest dto) {
        return adminCategoryService.createCategory(dto);
    }

    @Get("/{category_id}")
    public SingleDataResponse<CategoryData> getCategory(@Path("category_id") long categoryId) {
        return adminCategoryService.getCategory(categoryId);
    }

    @Get()
    public DataCollectionResponse<CategoryData> getAllCategory(@Query(value = "limit", required = false) Integer limit) {
        return adminCategoryService.getAllCategory(limit);
    }

    @Patch("/{category_id}")
    public IdResponse updateCategory(@Path("category_id") long categoryId, CategoryRequest dto) {
        return adminCategoryService.editCategory(categoryId, dto);
    }

    @Delete("/{category_id}")
    public DeleteResponse deleteCategory(@Path("category_id") long categoryId) {
        return adminCategoryService.deleteCategory(categoryId);
    }

}
