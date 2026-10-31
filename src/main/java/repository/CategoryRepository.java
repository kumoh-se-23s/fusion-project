package repository;

import model.domain.Category;

import java.util.List;
import java.util.Map;

public class CategoryRepository extends BaseRepositoryImpl<Category, Long>{
    public List<Category> findByPKs(List<Long> pks){
        return find(
                Filter.builder()
                        .conditions(Map.of("pk", pks))
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
