package service.internal;

import exception.NotFoundException;
import exception.NotNullException;
import lombok.RequiredArgsConstructor;
import model.domain.Category;
import model.dto.CategoryData;
import model.dto.request.PKQueryRequest;
import model.dto.request.category.CreateCategoryRequest;
import model.dto.response.PKResponse;
import model.dto.response.SingleDataResponse;
import repository.CategoryRepository;
import service.BaseService;


@RequiredArgsConstructor
public class CategoryService extends BaseService {
    private final CategoryRepository categoryRepository;

    public PKResponse createCategory(CreateCategoryRequest dto) {
        return tx.execute(() -> {
            Category category;
            try {
                category = new Category().toBuilder()
                        .name(dto.getName())
                        .description(dto.getDescription())
                        .build();
            } catch (NullPointerException e) {
                throw new NotNullException("Category has Null Value");
            }
            Long categoryPK = this.categoryRepository.create(category).getPk();
            return new PKResponse(categoryPK);
        });
    }

    public SingleDataResponse<CategoryData> readCategory(PKQueryRequest request) {
        return tx.execute(true, () -> {
            Category category = categoryRepository.findByPK(request.getPk())
                    .orElseThrow(NotFoundException::new);

            CategoryData dto = CategoryData.builder()
                    .pk(category.getPk())
                    .name(category.getName())
                    .description(category.getDescription())
                    .build();

            return new SingleDataResponse<>(dto);
        });
    }



}
