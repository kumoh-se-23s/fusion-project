package controller.internal;

import com.fasterxml.jackson.databind.JsonNode;
import exception.AlreadyExistException;
import exception.NotFoundException;
import fixture.AdminProjectFixtures;
import model.domain.Category;
import model.dto.CategoryData;
import model.dto.request.category.CategoryRequest;
import model.dto.response.DataCollectionResponse;
import model.dto.response.IdResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import repository.CategoryRepository;
import service.internal.AdminCategoryService;
import support.ApiEndpoint;
import support.ApiResponse;
import support.ApiTest;

import java.util.Map;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AdminCategoryRouter (/internal/category)")
class AdminCategoryRouterTest extends ApiTest {

    private Category createCategory() {
        return AdminProjectFixtures.category();
    }

    private Category reload(long id) {
        return bean(CategoryRepository.class).findById(id).orElseThrow();
    }

    private static boolean containsId(JsonNode datas, long id) {
        return StreamSupport.stream(datas.spliterator(), false)
                .anyMatch(node -> node.path("id").asLong() == id);
    }

    @Nested
    @ApiEndpoint(method = "POST", path = "/internal/category")
    @DisplayName("POST /internal/category - 카테고리 생성")
    class CreateCategory {

        @Nested
        @DisplayName("Http")
        class Http {
            @Test
            @DisplayName("200과 새 카테고리 id를 돌려주고 DB에 저장된다")
            void createsCategory() {
                String name = unique("category");

                ApiResponse response = client.post("/internal/category")
                        .json(Map.of("name", name, "description", "설명"))
                        .send().assertStatus(200);

                long id = response.json("/id").asLong();
                assertTrue(id > 0, response::toString);
                Category saved = reload(id);
                assertEquals(name, saved.getName());
                assertEquals("설명", saved.getDescription());
            }

            @Test
            @DisplayName("이미 있는 이름이면 409")
            void duplicateName() {
                Category existing = createCategory();

                client.post("/internal/category")
                        .json(Map.of("name", existing.getName(), "description", "중복"))
                        .send().assertStatus(409);
            }

            @Test
            @DisplayName("body가 없으면 422")
            void missingBody() {
                client.post("/internal/category").send().assertStatus(422);
            }

            @Test
            @DisplayName("깨진 JSON이면 422")
            void brokenJson() {
                client.post("/internal/category").rawJson("{\"name\": ").send().assertStatus(422);
            }

            @Test
            @DisplayName("[BUG] 이름 없이 보내면 400")
            void missingName() {
                // isExistByName(null)이 Map.of("name", null)을 만들어 NPE -> 500이 된다
                client.post("/internal/category")
                        .json(Map.of("description", "이름 없음"))
                        .send().assertStatus(400);
            }
        }

        @Nested
        @DisplayName("Logic")
        class Logic {
            @Test
            @DisplayName("이름과 설명을 그대로 저장한다")
            void savesFields() {
                String name = unique("category");

                IdResponse result = bean(AdminCategoryService.class).createCategory(
                        CategoryRequest.builder().name(name).description("설명").build());

                Category saved = reload(result.getId());
                assertEquals(name, saved.getName());
                assertEquals("설명", saved.getDescription());
            }

            @Test
            @DisplayName("같은 이름이 있으면 AlreadyExistException이고 행이 늘지 않는다")
            void duplicateName() {
                Category existing = createCategory();

                assertThrows(AlreadyExistException.class, () -> bean(AdminCategoryService.class).createCategory(
                        CategoryRequest.builder().name(existing.getName()).description("중복").build()));
                assertEquals(1, bean(CategoryRepository.class).findByName(existing.getName()).size());
            }
        }
    }

    @Nested
    @ApiEndpoint(method = "GET", path = "/internal/category/{category_id}")
    @DisplayName("GET /internal/category/{category_id} - 카테고리 단건 조회")
    class GetCategory {

        @Nested
        @DisplayName("Http")
        class Http {
            @Test
            @DisplayName("200과 카테고리 데이터를 돌려준다")
            void returnsCategory() {
                Category saved = createCategory();

                ApiResponse response = client.get("/internal/category/" + saved.getId()).send().assertStatus(200);

                assertEquals(saved.getId().longValue(), response.json("/data/id").asLong());
                assertEquals(saved.getName(), response.json("/data/name").asText());
                assertEquals(saved.getDescription(), response.json("/data/description").asText());
            }

            @Test
            @DisplayName("없는 id면 404")
            void notFound() {
                client.get("/internal/category/" + Long.MAX_VALUE).send().assertStatus(404);
            }

            @Test
            @DisplayName("id가 숫자가 아니면 422")
            void badPathType() {
                client.get("/internal/category/abc").send().assertStatus(422);
            }
        }

        @Nested
        @DisplayName("Logic")
        class Logic {
            @Test
            @DisplayName("DB의 값을 DTO로 옮긴다")
            void mapsFields() {
                Category saved = createCategory();

                CategoryData data = bean(AdminCategoryService.class).getCategory(saved.getId()).getData();

                assertEquals(saved.getId(), data.getId());
                assertEquals(saved.getName(), data.getName());
                assertEquals(saved.getDescription(), data.getDescription());
            }

            @Test
            @DisplayName("없는 id면 NotFoundException")
            void notFound() {
                assertThrows(NotFoundException.class,
                        () -> bean(AdminCategoryService.class).getCategory(Long.MAX_VALUE));
            }
        }
    }

    @Nested
    @ApiEndpoint(method = "GET", path = "/internal/category")
    @DisplayName("GET /internal/category - 카테고리 전체 조회")
    class GetAllCategory {

        @Nested
        @DisplayName("Http")
        class Http {
            @Test
            @DisplayName("limit 없이 200과 방금 만든 카테고리를 포함한 목록을 돌려준다")
            void returnsAll() {
                Category saved = createCategory();

                ApiResponse response = client.get("/internal/category").send().assertStatus(200);

                JsonNode datas = response.json("/datas");
                assertTrue(datas.isArray(), response::toString);
                assertEquals(datas.size(), response.json("/amount").asLong());
                assertTrue(containsId(datas, saved.getId()), () -> "방금 만든 카테고리가 응답에 없다: " + response);
            }

            @Test
            @DisplayName("limit만큼만 돌려준다")
            void limit() {
                createCategory();
                createCategory();

                ApiResponse response = client.get("/internal/category").query("limit", 1).send().assertStatus(200);

                assertEquals(1, response.json("/datas").size());
                assertEquals(1, response.json("/amount").asLong());
            }

            @Test
            @DisplayName("limit이 숫자가 아니면 422")
            void badLimit() {
                client.get("/internal/category").query("limit", "abc").send().assertStatus(422);
            }
        }

        @Nested
        @DisplayName("Logic")
        class Logic {
            @Test
            @DisplayName("limit이 null이면 DB의 모든 카테고리를 돌려준다")
            void allRows() {
                createCategory();

                DataCollectionResponse<CategoryData> result = bean(AdminCategoryService.class).getAllCategory(null);

                assertEquals(bean(CategoryRepository.class).findAll().size(), result.getAmount());
                assertEquals(result.getAmount(), (long) result.getDatas().size());
            }

            @Test
            @DisplayName("limit을 주면 그 수 이하로 돌려준다")
            void limited() {
                createCategory();
                createCategory();

                DataCollectionResponse<CategoryData> result = bean(AdminCategoryService.class).getAllCategory(2);

                assertEquals(2L, result.getAmount());
                assertEquals(2, result.getDatas().size());
            }
        }
    }

    @Nested
    @ApiEndpoint(method = "PATCH", path = "/internal/category/{category_id}")
    @DisplayName("PATCH /internal/category/{category_id} - 카테고리 수정")
    class UpdateCategory {

        @Nested
        @DisplayName("Http")
        class Http {
            @Test
            @DisplayName("200과 id를 돌려주고 이름과 설명이 바뀐다")
            void updates() {
                Category saved = createCategory();
                String newName = unique("renamed");

                ApiResponse response = client.patch("/internal/category/" + saved.getId())
                        .json(Map.of("name", newName, "description", "바뀐 설명"))
                        .send().assertStatus(200);

                assertEquals(saved.getId().longValue(), response.json("/id").asLong());
                ApiResponse after = client.get("/internal/category/" + saved.getId()).send().assertStatus(200);
                assertEquals(newName, after.json("/data/name").asText());
                assertEquals("바뀐 설명", after.json("/data/description").asText());
            }

            @Test
            @DisplayName("다른 카테고리와 이름이 겹치면 409")
            void duplicateName() {
                Category saved = createCategory();
                Category other = createCategory();

                client.patch("/internal/category/" + saved.getId())
                        .json(Map.of("name", other.getName()))
                        .send().assertStatus(409);
                assertEquals(saved.getName(), reload(saved.getId()).getName());
            }

            @Test
            @DisplayName("없는 id면 404")
            void notFound() {
                client.patch("/internal/category/" + Long.MAX_VALUE)
                        .json(Map.of("name", unique("none")))
                        .send().assertStatus(404);
            }

            @Test
            @DisplayName("body가 없으면 422")
            void missingBody() {
                Category saved = createCategory();
                client.patch("/internal/category/" + saved.getId()).send().assertStatus(422);
            }

            @Test
            @DisplayName("id가 숫자가 아니면 422")
            void badPathType() {
                client.patch("/internal/category/abc")
                        .json(Map.of("name", unique("x")))
                        .send().assertStatus(422);
            }

            @Test
            @DisplayName("[BUG] 설명만 보내면 설명만 바뀐다")
            void descriptionOnly() {
                // editCategory가 name이 null인데도 isExistByName(null) -> Map.of("name", null) NPE -> 500
                Category saved = createCategory();

                client.patch("/internal/category/" + saved.getId())
                        .json(Map.of("description", "설명만 수정"))
                        .send().assertStatus(200);

                Category after = reload(saved.getId());
                assertEquals(saved.getName(), after.getName());
                assertEquals("설명만 수정", after.getDescription());
            }
        }

        @Nested
        @DisplayName("Logic")
        class Logic {
            @Test
            @DisplayName("보낸 필드만 바꾼다")
            void updatesFields() {
                Category saved = createCategory();
                String newName = unique("renamed");

                IdResponse result = bean(AdminCategoryService.class).editCategory(saved.getId(),
                        CategoryRequest.builder().name(newName).build());

                assertEquals(saved.getId(), result.getId());
                Category after = reload(saved.getId());
                assertEquals(newName, after.getName());
                assertEquals(saved.getDescription(), after.getDescription());
            }

            @Test
            @DisplayName("이름이 겹치면 AlreadyExistException")
            void duplicateName() {
                Category saved = createCategory();
                Category other = createCategory();

                assertThrows(AlreadyExistException.class, () -> bean(AdminCategoryService.class).editCategory(
                        saved.getId(), CategoryRequest.builder().name(other.getName()).build()));
            }

            @Test
            @DisplayName("없는 id면 NotFoundException")
            void notFound() {
                assertThrows(NotFoundException.class, () -> bean(AdminCategoryService.class).editCategory(
                        Long.MAX_VALUE, CategoryRequest.builder().name(unique("none")).build()));
            }
        }
    }

    @Nested
    @ApiEndpoint(method = "DELETE", path = "/internal/category/{category_id}")
    @DisplayName("DELETE /internal/category/{category_id} - 카테고리 삭제")
    class DeleteCategory {

        @Nested
        @DisplayName("Http")
        class Http {
            @Test
            @DisplayName("200과 result=true를 돌려주고 이후 조회는 404")
            void deletes() {
                Category saved = createCategory();

                ApiResponse response = client.delete("/internal/category/" + saved.getId()).send().assertStatus(200);

                assertTrue(response.json("/result").asBoolean(), response::toString);
                client.get("/internal/category/" + saved.getId()).send().assertStatus(404);
            }

            @Test
            @DisplayName("없는 id면 404")
            void notFound() {
                client.delete("/internal/category/" + Long.MAX_VALUE).send().assertStatus(404);
            }

            @Test
            @DisplayName("id가 숫자가 아니면 422")
            void badPathType() {
                client.delete("/internal/category/abc").send().assertStatus(422);
            }
        }

        @Nested
        @DisplayName("Logic")
        class Logic {
            @Test
            @DisplayName("DB에서 행이 사라진다")
            void removesRow() {
                Category saved = createCategory();

                assertTrue(bean(AdminCategoryService.class).deleteCategory(saved.getId()).getResult());
                assertTrue(bean(CategoryRepository.class).findById(saved.getId()).isEmpty());
            }

            @Test
            @DisplayName("없는 id면 NotFoundException")
            void notFound() {
                assertThrows(NotFoundException.class,
                        () -> bean(AdminCategoryService.class).deleteCategory(Long.MAX_VALUE));
            }
        }
    }
}
