package service.external;

import lombok.RequiredArgsConstructor;
import model.Serializer;
import model.domain.Category;
import model.dto.CategoryData;
import model.dto.response.DataCollectionResponse;
import repository.CategoryRepository;
import service.BaseService;

import java.util.List;

@RequiredArgsConstructor
public class CategoryService extends BaseService {
    private final CategoryRepository categoryRepository;

    public DataCollectionResponse<CategoryData> getAllCategory() {
        return tx.execute(true, () -> {
            List<Category> categories = categoryRepository.findAll();

            return DataCollectionResponse.<CategoryData>builder()
                    .datas(Serializer.toDataList(categories, CategoryData.class))
                    .amount((long) categories.size())
                    .build();
        });
    }
}
