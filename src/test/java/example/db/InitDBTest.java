package example.db;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

//기본 데이터가 없으면 DatabaseTest의 InitDB.load에서 실패한다
@DisplayName("기본 데이터")
class InitDBTest extends DatabaseTest {

    @Test
    @DisplayName("기본 데이터를 불러와 전체 펀딩을 출력한다")
    void load() {
        System.out.println(db.config.getFundingService().getAllFunding().toJsonString());
    }
}
