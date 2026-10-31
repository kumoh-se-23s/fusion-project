package annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Column {
    String name();

    boolean nullable() default true;

    boolean unique() default false;

    //복합 유니크용
    String uniqueGroup() default "";

    int length() default 255;

    //BigDecimal의 DECIMAL(precision, scale)
    int precision() default 19;

    int scale() default 2;

    //직접 지정할 SQL 타입 (예: "TEXT"). 지정하면 위 타입 속성은 무시된다
    String columnDefinition() default "";
}
