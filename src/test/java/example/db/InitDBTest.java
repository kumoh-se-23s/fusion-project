package example.db;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@DisplayName("기본 데이터")
class InitDBTest extends DatabaseTest {
    private static final Logger LOG = LoggerFactory.getLogger(InitDBTest.class);

    @Test
    @DisplayName("전체 프로젝트를 출력한다")
    void load() {
        LOG.info(db.config.getProjectQueryService().getAllProject().toJsonString());
    }
}
