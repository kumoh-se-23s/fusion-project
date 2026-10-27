package service;

import exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import model.domain.Category;
import model.dto.CategoryData;
import model.dto.request.category.CreateCategoryRequest;
import model.dto.response.PKResponse;
import model.dto.response.SingleDataResponse;
import repository.CategoryRepository;

@RequiredArgsConstructor
public class CategoryService extends BaseService {
    private final CategoryRepository categoryRepository;

    public PKResponse createCategory(CreateCategoryRequest dto) {

        Category category = new Category().toBuilder()
                    .name(dto.getName())
                    .description(dto.getDescription())
                    .build();
        Long categoryPK = this.categoryRepository.create(category).getPk();
        return new PKResponse(categoryPK);
    }

    public SingleDataResponse<CategoryData> readCategory(Long categoryPK) {
        Category category = categoryRepository.findById(categoryPK)
                .orElseThrow(NotFoundException::new);

        CategoryData dto = CategoryData.builder()
                .pk(category.getPk())
                .name(category.getName())
                .description(category.getDescription())
                .build();
        //시리얼라이저 만들어 주세요......

        return new SingleDataResponse<>(dto);
    }

}
