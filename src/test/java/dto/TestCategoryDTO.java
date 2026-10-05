package dto;

import model.domain.Category;

import model.dto.Serializer;
import model.dto.category.RequestCategoryDTO;
import exception.SerializeException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TestCategoryDTO {

    private static RequestCategoryDTO request(String name, String description) {
        return RequestCategoryDTO.builder().name(name).description(description).build();
    }

    // ---------- DTO -> 도메인 ----------

    @Test
    @DisplayName("요청 DTO를 도메인으로 변환한다. pk는 DTO에 없으므로 비어 있다")
    void toDomain_copiesFields_andLeavesPkNull() {
        Category category = request("food", "음식 펀딩").toDomain(Category.class);

        assertEquals("food", category.getName());
        assertEquals("음식 펀딩", category.getDescription());
        assertNull(category.getPk());
    }

    @Test
    @DisplayName("nullable 필드가 null이어도 변환된다")
    void toDomain_succeeds_whenDescriptionIsNull() {
        Category category = request("food", null).toDomain(Category.class);

        assertEquals("food", category.getName());
        assertNull(category.getDescription());
    }

    @Test
    @DisplayName("DTO의 NotNull은 Serializer가 읽지 않으므로 name이 null이어도 변환된다")
    void toDomain_doesNotEnforceNotNull() {
        // NotNull 검사는 도메인 저장 직전 검증기의 몫이다. 그 검증기를 만들면 이 테스트는 삭제한다.
        Category category = request(null, "desc").toDomain(Category.class);

        assertNull(category.getName());
    }

    @Test
    @DisplayName("허용되지 않은 도메인 타입으로 변환하면 예외가 발생한다")
    void toDomain_throws_whenDomainTypeIsNotBound() {
        RequestCategoryDTO dto = request("food", null);

        assertThrows(SerializeException.class, () -> dto.toDomain(String.class));
    }

    @Test
    @DisplayName("DTO 리스트를 도메인 리스트로 변환한다")
    void toDomains_convertsList() {
        List<RequestCategoryDTO> dtos = List.of(request("a", "descA"), request("b", "descB"));

        List<Category> categories = Serializer.toDomains(dtos, Category.class);

        assertEquals(2, categories.size());
        assertEquals("a", categories.get(0).getName());
    }

    @Test
    @DisplayName("DTO 리스트의 null 요소는 건너뛴다")
    void toDomains_skipsNullElements() {
        List<Category> categories =
            Serializer.toDomains(Arrays.asList(request("a", null), null), Category.class);

        assertEquals(1, categories.size());
    }

    @Test
    @DisplayName("허용되지 않은 도메인 타입으로 리스트를 변환하면 예외가 발생한다")
    void toDomains_throws_whenDomainTypeIsNotBound() {
        List<RequestCategoryDTO> dtos = List.of(request("a", null));

        assertThrows(SerializeException.class, () -> Serializer.toDomains(dtos, String.class));
    }

    @Test
    @DisplayName("도메인 타입을 인자로 넘기므로 바로 체이닝할 수 있다")
    void toDomain_canBeChained() {
        assertEquals("food", request("food", null).toDomain(Category.class).getName());
    }

    // ---------- 도메인 -> DTO ----------

    @Test
    @DisplayName("도메인에서 DTO로 변환한다. DTO에 없는 pk는 버려진다")
    void fromDomain_copiesFields() {
        RequestCategoryDTO dto = new RequestCategoryDTO().fromDomain(new Category(1L, "test", "description"));

        assertNotNull(dto);
        assertEquals("test", dto.getName());
        assertEquals("description", dto.getDescription());
    }

    @Test
    @DisplayName("null 입력은 예외 없이 null을 반환한다")
    void fromDomain_returnsNull_whenInputIsNull() {
        assertNull(new RequestCategoryDTO().fromDomain(null));
        assertNull(Serializer.fromDomain(null, RequestCategoryDTO.class));
    }

    @Test
    @DisplayName("바인딩되지 않은 도메인은 예외가 발생한다")
    void fromDomain_throws_whenDomainIsNotBound() {
        assertThrows(SerializeException.class, () -> new RequestCategoryDTO().fromDomain("문자열"));
    }

    @Test
    @DisplayName("static 방식도 인스턴스 방식과 동일하게 동작한다")
    void staticFromDomain_worksSameAsInstanceMethod() {
        assertNotNull(Serializer.fromDomain(new Category(1L, "test", null), RequestCategoryDTO.class));
    }

    @Test
    @DisplayName("도메인 리스트의 null 요소는 건너뛴다")
    void fromDomains_skipsNullElements() {
        List<Category> categories = Arrays.asList(new Category(1L, "a", null), null, new Category(2L, "b", null));

        List<RequestCategoryDTO> dtos = new RequestCategoryDTO().fromDomains(categories);

        assertEquals(2, dtos.size());
    }

    @Test
    @DisplayName("리스트에 바인딩되지 않은 요소가 섞이면 전체가 예외로 끝난다")
    void fromDomains_throws_whenListContainsUnboundElement() {
        List<Object> mixed = Arrays.asList(new Category(1L, "a", null), "문자열");

        assertThrows(SerializeException.class, () -> new RequestCategoryDTO().fromDomains(mixed));
    }

    // ---------- 왕복 ----------

    @Test
    @DisplayName("도메인 -> DTO -> 도메인 왕복에서 pk만 사라진다")
    void roundTrip_losesOnlyPk() {
        Category original = new Category(1L, "test", "description");

        Category restored = new RequestCategoryDTO().fromDomain(original).toDomain(Category.class);

        assertNull(restored.getPk());
        assertEquals(original.getName(), restored.getName());
        assertEquals(original.getDescription(), restored.getDescription());
    }
}