package repository.manager.query_dsl;

import annotation.Column;
import annotation.JoinColumn;
import annotation.PK;
import annotation.Table;
import annotation.Version;
import exception.AnnotationMissingException;
import exception.QueryErrorException;
import lombok.Getter;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * DB 테이블에 대응되는 데이터를 저장하는 클래스
 * CACHE : 쿼리를 넣을때 마다 metadata를 생성하는 것은 비효율적 -> 실행될때 만들어서 지속적 사용
 * version : 낙관적 락을 위한 버전 필드
 *
 */
@Getter
public class EntityMetadata {
    //객체마다 정해진 필드 데이터를 캐싱하는 배열
    private static final Map<Class<?>, EntityMetadata> CACHE = new ConcurrentHashMap<>();

    private final Class<?> entityClass;
    private final String tableName;
    private final ColumnInfo pk;
    private final boolean autoIncrement;
    private final ColumnInfo version;
    private final List<ColumnInfo> columns;
    private final List<ColumnInfo> updatableColumns;
    private final List<ColumnInfo> insertColumns;
    private final Map<String, ColumnInfo> columnsByField;

    private final String insertSql;
    private final String updateSql;
    private final String selectByPkSql;
    private final String deleteSql;

    public static EntityMetadata of(Class<?> entityClass) {
        return CACHE.computeIfAbsent(entityClass, EntityMetadata::new);
    }

    private EntityMetadata(Class<?> entityClass) {
        Table table = entityClass.getAnnotation(Table.class);
        if (table == null) {
            throw new AnnotationMissingException("@Table is missing: " + entityClass.getName());
        }
        this.entityClass = entityClass;
        this.tableName = table.name();

        ColumnInfo pkInfo = null;
        ColumnInfo versionInfo = null;
        List<ColumnInfo> cols = new ArrayList<>();

        for (Class<?> c = entityClass; c != null && c != Object.class; c = c.getSuperclass()) {
            for (Field field : c.getDeclaredFields()) {
                ColumnInfo info = toColumnInfo(field);
                if (info == null) {
                    if (field.isAnnotationPresent(PK.class)) {
                        throw new AnnotationMissingException("@PK field needs @Column: " + field);
                    }
                    continue;
                }
                if (field.isAnnotationPresent(PK.class)) {
                    if (pkInfo != null) throw new AnnotationMissingException("Multiple @PK: " + entityClass.getName());
                    pkInfo = info;
                } else {
                    cols.add(info);
                    if (field.isAnnotationPresent(Version.class)) {
                        if (versionInfo != null) throw new AnnotationMissingException("Multiple @Version: " + entityClass.getName());
                        versionInfo = info;
                    }
                }
            }
        }
        if (pkInfo == null) {
            throw new AnnotationMissingException("@PK is missing: " + entityClass.getName());
        }

        this.pk = pkInfo;
        this.autoIncrement = pkInfo.field().getAnnotation(PK.class).autoIncrement();
        this.version = versionInfo;
        this.columns = List.copyOf(cols);
        final ColumnInfo v = versionInfo;
        this.updatableColumns = cols.stream().filter(col -> col != v).toList();

        //PK를 DB가 생성하지 않으면 INSERT에 PK도 포함
        List<ColumnInfo> inserts = new ArrayList<>();
        if (!autoIncrement) inserts.add(pkInfo);
        inserts.addAll(cols);
        this.insertColumns = List.copyOf(inserts);

        Map<String, ColumnInfo> byField = new HashMap<>();
        byField.put(pkInfo.field().getName(), pkInfo);
        cols.forEach(col -> byField.put(col.field().getName(), col));
        this.columnsByField = Map.copyOf(byField);

        this.insertSql = buildInsertSql();
        this.updateSql = buildUpdateSql();
        this.selectByPkSql = "SELECT * FROM " + tableName + " WHERE " + pk.columnName() + " = ?";
        this.deleteSql = "DELETE FROM " + tableName + " WHERE " + pk.columnName() + " = ?";
    }

    //필드명으로 컬럼 정보 조회 (Filter, Sort, update 맵의 키 검증용)
    public ColumnInfo getColumnByField(String fieldName) {
        ColumnInfo info = columnsByField.get(fieldName);
        if (info == null) {
            throw new QueryErrorException("Unknown field '" + fieldName + "' in " + entityClass.getSimpleName());
        }
        return info;
    }

    //PK 여러 개로 한 번에 조회 (연관 엔티티 일괄 로딩용)
    public String getSelectByPksSql(int count) {
        String marks = String.join(", ", Collections.nCopies(count, "?"));
        return "SELECT * FROM " + tableName + " WHERE " + pk.columnName() + " IN (" + marks + ")";
    }

    private static ColumnInfo toColumnInfo(Field field) {
        JoinColumn join = field.getAnnotation(JoinColumn.class);
        Column column = field.getAnnotation(Column.class);
        if (join == null && column == null) return null;

        if (join != null) {
            requireEntityType(field);
        }

        field.setAccessible(true);
        return join != null
                ? new ColumnInfo(field, join.name(), true)
                : new ColumnInfo(field, column.name(), false);
    }

    //@JoinColumn 필드는 @Table 엔티티 타입이어야 한다
    private static void requireEntityType(Field field) {
        if (!field.getType().isAnnotationPresent(Table.class)) {
            throw new AnnotationMissingException("@JoinColumn field must be an @Table entity: " + field);
        }
    }

    private String buildInsertSql() {
        String names = insertColumns.stream().map(ColumnInfo::columnName).collect(Collectors.joining(", "));
        String marks = insertColumns.stream().map(c -> "?").collect(Collectors.joining(", "));
        return "INSERT INTO " + tableName + " (" + names + ") VALUES (" + marks + ")";
    }


    private String buildUpdateSql() {
        List<String> sets = updatableColumns.stream()
                .map(c -> c.columnName() + " = ?")
                .collect(Collectors.toCollection(ArrayList::new));
        if (version != null) {
            sets.add(version.columnName() + " = " + version.columnName() + " + 1");
        }
        String where = pk.columnName() + " = ?";
        if (version != null) {
            where += " AND " + version.columnName() + " = ?";
        }
        return "UPDATE " + tableName + " SET " + String.join(", ", sets) + " WHERE " + where;
    }
}
