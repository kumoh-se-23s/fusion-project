package config;

import lombok.Getter;
import repository.manager.schema.SchemaGenerator;

/**
 * 앱 시작 시 리포지토리, 서비스를 한 번 생성하고 주입하는 조립 클래스
 * 새 서비스를 만들면 여기에 등록한다.
 */
@Getter
public class AppConfig {
    public AppConfig() {
        //db.ddl-auto 설정에 따라 도메인 기준으로 테이블 생성
        SchemaGenerator.Mode ddlMode = SchemaGenerator.Mode.from(DBProperties.get("db.ddl-auto", "none"));
        new SchemaGenerator().execute(ddlMode, "model.domain");
    }
}
