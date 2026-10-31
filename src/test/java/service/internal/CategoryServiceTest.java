package service.internal;

import example.db.DatabaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("CategoryService")
class CategoryServiceTest extends DatabaseTest {
    private CategoryService categoryService;

    @BeforeEach
    void setUp() {
        categoryService = db.config.getCategoryService();
    }

    @Nested
    @DisplayName("createCategory")
    class CreateCategory {
        @Test
        @DisplayName("카테고리를 생성한다")
        void success() {
        }

        @Test
        @DisplayName("이름이 없으면 NotNullException")
        void withoutName() {
        }
    }

    @Nested
    @DisplayName("readCategory")
    class ReadCategory {
        @Test
        @DisplayName("카테고리를 조회한다")
        void success() {
        }

        @Test
        @DisplayName("없는 카테고리면 NotFoundException")
        void notFound() {
        }
    }
}
