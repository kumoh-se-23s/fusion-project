package service.internal;

import exception.AlreadyExistException;
import exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import model.Serializer;
import model.domain.Category;
import model.dto.CategoryData;
import model.dto.request.category.CategoryRequest;
import model.dto.response.DataCollectionResponse;
import model.dto.response.DeleteResponse;
import model.dto.response.IdResponse;
import model.dto.response.SingleDataResponse;
import repository.CategoryRepository;
import service.BaseService;

import java.util.List;

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

            if (categoryRepository.isExistByName(dto.getName())) {
                throw new AlreadyExistException();
            }

            return new IdResponse(categoryRepository.create(category).getId());
        });
    }

    public SingleDataResponse<CategoryData> getCategory(long categoryId) {
        return tx.execute(true, () -> {

            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(NotFoundException::new);

            return new SingleDataResponse<>(Serializer.toData(category, CategoryData.class));

        });
    }

    public DataCollectionResponse<CategoryData> getAllCategory(int limit) {
        return tx.execute(true, () -> {
            List<Category> categories = categoryRepository.findAll(limit);

            return DataCollectionResponse.<CategoryData>builder()
                    .datas(Serializer.toDataList(categories, CategoryData.class))
                    .amount((long) categories.size())
                    .build();
        });
    }

    public IdResponse editCategory(Long categoryId, CategoryRequest dto) {
        return tx.execute(false, () -> {

            if (categoryRepository.isExistByName(dto.getName())) {
                throw new AlreadyExistException();
            }

            Category category = categoryRepository.update(categoryId, dto.toUpdateMap())
                    .orElseThrow(NotFoundException::new);



            return new IdResponse(category.getId());
        });
    }

    public DeleteResponse deleteCategory(Long categoryId) {
        return tx.execute(false, () ->{
            Category category =  categoryRepository.findById(categoryId)
                    .orElseThrow(NotFoundException::new);

            return new DeleteResponse(
                    categoryRepository.delete(categoryId)
            );
        });
    }



}
