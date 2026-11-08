package service.internal;

import exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import model.Serializer;
import model.domain.Category;
import model.dto.CategoryData;
import model.dto.request.IdQueryRequest;
import model.dto.request.category.CategoryRequest;
import model.dto.response.DataCollectionResponse;
import model.dto.response.DeleteResponse;
import model.dto.response.IdResponse;
import model.dto.response.SingleDataResponse;
import repository.CategoryRepository;
import service.BaseService;

/**
 * 관리자가 카테고리를 관리, 조회하는 서비스 클래스
 * 카테고리 관련 API는 그 수가 적으므로 클래스 분리를 진행하지 않았다
 * @since 2026-11-05
 * @author 김민규
 * @version 1.0
**/
@RequiredArgsConstructor
public class AdminCategoryService extends BaseService {
    private final CategoryRepository categoryRepository;

    public IdResponse createCategory(CategoryRequest dto) {
        return tx.execute(() -> {
            Category category = Serializer.toDomain(dto, Category.class);
            Long categoryId = this.categoryRepository.create(category).getId();
            return new IdResponse(categoryId);
        });
    }

    public SingleDataResponse<CategoryData> getCategory(IdQueryRequest request) {
        return tx.execute(true, () -> {

            Category category = categoryRepository.findById(request.getId())
                    .orElseThrow(NotFoundException::new);

            return new SingleDataResponse<>(Serializer.toData(category, CategoryData.class));

        });
    }

    public DataCollectionResponse<CategoryData> getAllCategory() {
        return null;
    }

    public IdResponse editCategory(CategoryRequest dto) {
        return null;
    }
    public DeleteResponse deleteCategory(Long CategoryId) {
        return null;
    }



}
