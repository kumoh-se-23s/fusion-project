package annotation;

/** 참조 대상(부모)이 삭제될 때 FK 컬럼을 가진 행의 동작 */
public enum OnDelete {
    //자식이 있으면 부모 삭제를 거부
    NONE(""),
    //자식도 함께 삭제
    CASCADE(" ON DELETE CASCADE"),
    //자식의 FK를 null로 변경. nullable 컬럼에만 사용 가능
    SET_NULL(" ON DELETE SET NULL");

    private final String sql;

    OnDelete(String sql) {
        this.sql = sql;
    }

    public String sql() {
        return sql;
    }
}
