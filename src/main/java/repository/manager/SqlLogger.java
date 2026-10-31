package repository.manager;

import config.PropertieData;

import java.util.List;
import java.util.StringJoiner;
import java.util.logging.Logger;

/**
 * 실행 SQL을 출력하는 로거 (java.util.logging)
 * 기본은 꺼져 있고 db.show-sql=true 로 켤 수 있다.
 * 문자열 파라미터에는 비밀번호, 이메일 등이 들어갈 수 있으므로 값을 출력하지 않는다.
 */
final class SqlLogger {
    private static final Logger LOG = Logger.getLogger("fusion.sql");
    private static final boolean ENABLED = PropertieData.getBoolean("db.show-sql", false);

    private SqlLogger() {
    }

    static void query(String sql, List<?> params, String result, long startNanos) {
        if (!ENABLED) return;
        long elapsedMs = (System.nanoTime() - startNanos) / 1_000_000;
        LOG.info("[SQL] " + sql + " | params=" + format(params) + " | " + result + " | " + elapsedMs + "ms");
    }

    private static String format(List<?> params) {
        StringJoiner joiner = new StringJoiner(", ", "[", "]");
        for (Object param : params) {
            if (param instanceof byte[] bytes) {
                joiner.add("<BLOB " + bytes.length + " bytes>");
            } else if (param instanceof CharSequence) {
                joiner.add("'***'");
            } else {
                joiner.add(String.valueOf(param));
            }
        }
        return joiner.toString();
    }
}
