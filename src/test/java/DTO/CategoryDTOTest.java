package DTO;

import DTO.Category.RequestCategoryDTO;
import Domain.Category;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public class CategoryDTOTest {

    static {
        System.setProperty("java.util.logging.SimpleFormatter.format", "[%4$s] %5$s%n");
    }

    private static final Logger LOGGER = Logger.getLogger(CategoryDTOTest.class.getName());

    public static void main(String[] args) {
        Category category = new Category(1L, "test", "description");

        // 1. 도메인 -> DTO
        RequestCategoryDTO dto = new RequestCategoryDTO().fromDomain(category);
        LOGGER.info("1. 도메인 -> DTO: " + dto);
        check("1", dto != null && "test".equals(dto.getName()) && "description".equals(dto.getDescription()));

        // 2. DTO -> 도메인 (DTO에 없는 pk는 복원되지 않음)
        Category restored = dto.toDomain(Category.class);
        LOGGER.info("2. DTO -> 도메인: " + restored);
        check("2", restored != null && "test".equals(restored.getName()));

        // 3. 리스트 변환
        List<Category> categories = List.of(
            new Category(1L, "a", "descA"),
            new Category(2L, "b", "descB")
        );
        List<RequestCategoryDTO> dtos = new RequestCategoryDTO().fromDomains(categories);
        LOGGER.info("3. fromDomains: " + dtos);
        check("3", dtos.size() == 2);

        // 4. null 입력은 조용히 null 반환
        check("4", new RequestCategoryDTO().fromDomain(null) == null);

        // 5. nullable 필드(description)가 null이어도 통과
        RequestCategoryDTO noDesc = new RequestCategoryDTO().fromDomain(new Category(3L, "c", null));
        check("5", noDesc != null && noDesc.getDescription() == null);

        // 6. @NotNull 필드(name)가 null이면 경고 로그 후 null
        LOGGER.info("6. @NotNull 위반 (아래 경고는 정상)");
        check("6", new RequestCategoryDTO().fromDomain(new Category(4L, null, "desc")) == null);

        // 7. 바인딩되지 않은 도메인 -> 경고 로그 후 null
        LOGGER.info("7. 바인딩 안 된 도메인 (아래 경고는 정상)");
        check("7", new RequestCategoryDTO().fromDomain("문자열") == null);

        // 8. DTO -> 허용되지 않은 도메인 타입 -> 경고 로그 후 null
        LOGGER.info("8. 허용 안 된 변환 대상 (아래 경고는 정상)");
        check("8", dto.toDomain(String.class) == null);

        // 9. 리스트에 null, 실패 항목이 섞이면 제외
        LOGGER.info("9. 실패 항목 제외 (아래 경고는 정상)");
        List<Category> mixed = Arrays.asList(
            new Category(5L, "ok", "d"),
            null,
            new Category(6L, null, "d")
        );
        check("9", new RequestCategoryDTO().fromDomains(mixed).size() == 1);

        // 10. DTO 리스트 -> 도메인 리스트
        List<Category> restoredList = Serializer.toDomains(dtos, Category.class);
        LOGGER.info("10. toDomains: " + restoredList);
        check("10", restoredList.size() == 2);

        // 11. 허용되지 않은 도메인 타입이면 전부 실패 -> 빈 리스트
        LOGGER.info("11. toDomains 전부 실패 (아래 경고는 정상)");
        check("11", Serializer.toDomains(dtos, String.class).isEmpty());

        // 12. DTO 리스트의 null 요소는 제외
        check("12", Serializer.toDomains(Arrays.asList(dtos.get(0), null), Category.class).size() == 1);

        // 13. static 방식도 동일하게 동작
        check("13", Serializer.fromDomain(category, RequestCategoryDTO.class) != null);

        // 14. 체이닝 (인자로 타입을 넘기므로 Object 추론 문제 없음)
        check("14", "test".equals(dto.toDomain(Category.class).getName()));

        LOGGER.info("모든 테스트 통과");
    }

    private static void check(String name, boolean condition) {
        if (condition) {
            LOGGER.fine("통과: " + name);
        } else {
            LOGGER.severe("테스트 실패: " + name);
            throw new AssertionError("테스트 실패: " + name);
        }
    }
}