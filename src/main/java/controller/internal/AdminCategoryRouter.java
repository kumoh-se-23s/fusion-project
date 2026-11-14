package controller.internal;

import model.dto.CategoryData;
import model.dto.request.IdQueryRequest;
import model.dto.request.category.CategoryRequest;
import model.dto.response.DataCollectionResponse;
import model.dto.response.DeleteResponse;
import model.dto.response.IdResponse;
import model.dto.response.SingleDataResponse;
import service.internal.AdminCategoryService;

/**
 * 카테고리 어드민 사용 시스템
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
    public DataCollectionResponse<CategoryData> getAllCategory(@Query("limit") int limit) {
        return adminCategoryService.getAllCategory(limit);
    }

    @Patch("{category_id}")
    public IdResponse updateCategory(@Path("category_id") long categoryId, CategoryRequest dto) {
        return adminCategoryService.editCategory(categoryId, dto);
    }

    @Delete("{categoryId")
    public DeleteResponse deleteCategory(@Path("category_id") long categoryId) {
        return adminCategoryService.deleteCategory(categoryId);
    }

}
