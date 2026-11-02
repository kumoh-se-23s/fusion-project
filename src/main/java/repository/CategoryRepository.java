package repository;

import model.domain.Category;

import java.util.List;
import java.util.Map;

public class CategoryRepository extends BaseRepositoryImpl<Category, Long>{
    public List<Category> findByIds(List<Long> ids){
        return find(
                Filter.builder()
                        .conditions(Map.of("id", ids))
                        .build()
        );
    }
    public List<Category> findByName(List<String> nameList) {
        return find(Filter.builder()
                .conditions(Map.of("name", nameList))
                .build()
        );
    }
}
