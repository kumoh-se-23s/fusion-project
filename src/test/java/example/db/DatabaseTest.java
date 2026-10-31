package example.db;

import org.junit.jupiter.api.BeforeEach;

/**
 * DB를 쓰는 테스트의 부모 클래스
 * InitDB.main으로 넣어둔 기본 데이터를 불러오기만 하고, DB를 지우지 않는다.
 */
public abstract class DatabaseTest {
    protected InitDB db;

    @BeforeEach
    protected void setUpDatabase() {
        db = InitDB.load();
    }
}
