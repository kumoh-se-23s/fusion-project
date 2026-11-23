package controller.external;

import com.fasterxml.jackson.databind.JsonNode;
import model.domain.Category;
import model.dto.CategoryData;
import model.dto.response.DataCollectionResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import repository.CategoryRepository;
import service.external.CategoryService;
import support.ApiEndpoint;
import support.ApiResponse;
import support.ApiTest;

import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CategoryRouter (/category)")
class CategoryRouterTest extends ApiTest {

    private Category createCategory() {
        String name = unique("category");
        return bean(CategoryRepository.class).create(Category.builder()
                .name(name)
                .description(name + " 설명")
                .build());
    }

    @Nested
    @ApiEndpoint(method = "GET", path = "/category")
    @DisplayName("GET /category - 전체 카테고리 조회")
    class GetAllCategory {

        @Nested
        @DisplayName("Http")
        class Http {
            @Test
            @DisplayName("로그인 없이 200과 카테고리 목록을 돌려준다")
            void returnsAllCategories() {
                Category saved = createCategory();

                ApiResponse response = client.get("/category").send().assertStatus(200);

                JsonNode datas = response.json("/datas");
                assertTrue(datas.isArray(), response::toString);
                assertEquals(datas.size(), response.json("/amount").asLong());
                assertTrue(StreamSupport.stream(datas.spliterator(), false)
                                .anyMatch(node -> node.path("name").asText().equals(saved.getName())),
                        () -> "방금 만든 카테고리가 응답에 없다: " + response);
            }

            @Test
            @DisplayName("허용하지 않은 메서드는 405")
            void wrongMethod() {
                client.delete("/category").send().assertStatus(405);
            }
        }

        @Nested
        @DisplayName("Logic")
        class Logic {
            @Test
            @DisplayName("DB에 있는 카테고리 수와 amount가 같다")
            void amountMatchesRows() {
                createCategory();

                DataCollectionResponse<CategoryData> result = bean(CategoryService.class).getAllCategory();

                assertEquals(bean(CategoryRepository.class).findAll().size(), result.getAmount());
                assertEquals(result.getAmount(), (long) result.getDatas().size());
            }

            @Test
            @DisplayName("이름과 설명이 그대로 DTO로 옮겨진다")
            void mapsFields() {
                Category saved = createCategory();

                CategoryData data = bean(CategoryService.class).getAllCategory().getDatas().stream()
                        .filter(d -> d.getId().equals(saved.getId()))
                        .findFirst()
                        .orElseThrow();

                assertEquals(saved.getName(), data.getName());
                assertEquals(saved.getDescription(), data.getDescription());
            }
        }
    }
}
