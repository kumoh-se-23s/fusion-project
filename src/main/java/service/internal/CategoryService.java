package service.internal;

import exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import model.Serializer;
import model.domain.Category;
import model.dto.CategoryData;
import model.dto.request.IdQueryRequest;
import model.dto.request.category.CreateCategoryRequest;
import model.dto.response.IdResponse;
import model.dto.response.SingleDataResponse;
import repository.CategoryRepository;
import service.BaseService;


@RequiredArgsConstructor
public class CategoryService extends BaseService {
    private final CategoryRepository categoryRepository;

    public IdResponse createCategory(CreateCategoryRequest dto) {
        return tx.execute(() -> {
            Category category = Serializer.toDomain(dto, Category.class);
            Long categoryId = this.categoryRepository.create(category).getId();
            return new IdResponse(categoryId);
        });
    }

    public SingleDataResponse<CategoryData> readCategory(IdQueryRequest request) {
        return tx.execute(true, () -> {

            Category category = categoryRepository.findById(request.getId())
                    .orElseThrow(NotFoundException::new);

            return new SingleDataResponse<>(Serializer.toData(category, CategoryData.class));

        });
    }



}
