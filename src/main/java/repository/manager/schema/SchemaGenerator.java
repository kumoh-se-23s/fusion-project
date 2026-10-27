package repository.manager.schema;

import annotation.Column;
import annotation.JoinColumn;
import config.PooledDataSource;
import exception.AnnotationMissingException;
import exception.TransactionException;
import repository.manager.SQLExecutor;
import repository.manager.query_dsl.ColumnInfo;
import repository.manager.query_dsl.EntityMetadata;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 도메인 어노테이션(@Table, @Column, @JoinColumn, @PK, @Version)을 읽어 MySQL 테이블을 생성하는 클래스
 *
 * 모드 (db.ddl-auto)
 *   none              : 아무것도 하지 않음
 *   create            : 기존 테이블을 DROP 후 다시 생성 (데이터 삭제됨)
 *   create-if-missing : 없는 테이블만 생성 (기존 테이블 구조는 바꾸지 않음)
 */
public class SchemaGenerator {

    public enum Mode {
        NONE, CREATE, CREATE_IF_MISSING;

        public static Mode from(String value) {
            return valueOf(value.trim().toUpperCase().replace('-', '_'));
        }
    }

    public void execute(Mode mode, String basePackage) {
        if (mode == Mode.NONE) {
            return;
        }
        List<Class<?>> entities = EntityScanner.scan(basePackage);

        try (Connection connection = PooledDataSource.getDataSource().getConnection()) {
            //FK가 걸린 테이블도 순서와 상관없이 DROP/CREATE 할 수 있도록 검사를 잠시 끔
            SQLExecutor.update(connection, "SET FOREIGN_KEY_CHECKS = 0", List.of());
            try {
                if (mode == Mode.CREATE) {
                    for (Class<?> entity : entities) {
                        SQLExecutor.update(connection, "DROP TABLE IF EXISTS " + EntityMetadata.of(entity).getTableName(), List.of());
                    }
                }
                for (Class<?> entity : entities) {
                    SQLExecutor.update(connection, createTableSql(entity, mode == Mode.CREATE_IF_MISSING), List.of());
                }
            } finally {
                SQLExecutor.update(connection, "SET FOREIGN_KEY_CHECKS = 1", List.of());
            }
        } catch (SQLException e) {
            throw new TransactionException("Failed to get connection", e);
        }
    }

    public String createTableSql(Class<?> entity, boolean ifNotExists) {
        EntityMetadata metadata = EntityMetadata.of(entity);
        String table = metadata.getTableName();
        List<String> lines = new ArrayList<>();
        List<String> constraints = new ArrayList<>();
        Map<String, List<String>> uniqueGroups = new LinkedHashMap<>();

        ColumnInfo pk = metadata.getPk();
        lines.add(pk.columnName() + " " + sqlType(pk) + " NOT NULL" + (metadata.isAutoIncrement() ? " AUTO_INCREMENT" : ""));
        constraints.add("PRIMARY KEY (" + pk.columnName() + ")");

        for (ColumnInfo column : metadata.getColumns()) {
            if (column == metadata.getVersion()) {
                lines.add(column.columnName() + " BIGINT NOT NULL DEFAULT 0");
                continue;
            }
            lines.add(column.columnName() + " " + sqlType(column) + (isNullable(column) ? "" : " NOT NULL"));

            if (isUnique(column)) {
                constraints.add("UNIQUE KEY uk_" + table.toLowerCase() + "_" + column.columnName()
                        + " (" + column.columnName() + ")");
            }
            String group = uniqueGroup(column);
            if (!group.isEmpty()) {
                uniqueGroups.computeIfAbsent(group, g -> new ArrayList<>()).add(column.columnName());
            }
            if (column.isRelation()) {
                EntityMetadata target = EntityMetadata.of(column.getRelationClass());
                constraints.add("CONSTRAINT fk_" + table.toLowerCase() + "_" + column.columnName()
                        + " FOREIGN KEY (" + column.columnName() + ")"
                        + " REFERENCES " + target.getTableName() + " (" + target.getPk().columnName() + ")");
            }
        }

        uniqueGroups.forEach((group, columns) ->
                constraints.add("UNIQUE KEY uk_" + table.toLowerCase() + "_" + group
                        + " (" + String.join(", ", columns) + ")"));

        lines.addAll(constraints);
        return "CREATE TABLE " + (ifNotExists ? "IF NOT EXISTS " : "") + table + " (\n    "
                + String.join(",\n    ", lines)
                + "\n) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4";
    }

    private static String sqlType(ColumnInfo column) {
        if (column.isRelation()) {
            //FK 컬럼은 참조 대상 PK와 같은 타입
            return sqlType(EntityMetadata.of(column.getRelationClass()).getPk());
        }
        Column annotation = column.field().getAnnotation(Column.class);
        if (!annotation.columnDefinition().isEmpty()) {
            return annotation.columnDefinition();
        }

        Class<?> type = column.getTargetClass();
        if (type == Long.class || type == long.class) return "BIGINT";
        if (type == Integer.class || type == int.class) return "INT";
        if (type == Boolean.class || type == boolean.class) return "BOOLEAN";
        if (type == Double.class || type == double.class) return "DOUBLE";
        if (type == String.class || type.isEnum()) return "VARCHAR(" + annotation.length() + ")";
        if (type == BigDecimal.class) return "DECIMAL(" + annotation.precision() + ", " + annotation.scale() + ")";
        if (type == LocalDateTime.class) return "DATETIME(6)";
        if (type == LocalDate.class) return "DATE";
        if (type == byte[].class) return "LONGBLOB";
        throw new AnnotationMissingException(
                "No SQL type for " + type.getSimpleName() + " (" + column.field() + "). Use @Column(columnDefinition = ...)");
    }

    private static boolean isNullable(ColumnInfo column) {
        return column.isRelation()
                ? column.field().getAnnotation(JoinColumn.class).nullable()
                : column.field().getAnnotation(Column.class).nullable();
    }

    private static boolean isUnique(ColumnInfo column) {
        return column.isRelation()
                ? column.field().getAnnotation(JoinColumn.class).unique()
                : column.field().getAnnotation(Column.class).unique();
    }

    private static String uniqueGroup(ColumnInfo column) {
        return column.isRelation()
                ? column.field().getAnnotation(JoinColumn.class).uniqueGroup()
                : column.field().getAnnotation(Column.class).uniqueGroup();
    }
}
