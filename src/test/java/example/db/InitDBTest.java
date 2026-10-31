package example.db;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("기본 데이터")
class InitDBTest extends DatabaseTest {

    @Test
    @DisplayName("InitDB.main으로 넣은 기본 데이터를 불러온다")
    void load() {
        assertAll(
                () -> assertNotNull(db.maker.getPk()),
                () -> assertNotNull(db.food.getPk()),
                () -> assertNotNull(db.pendingFunding.getPk()),
                () -> assertNotNull(db.openReward.getPk())
        );
    }
}
