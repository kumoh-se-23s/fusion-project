package annotation;

import java.lang.annotation.*;

/**
 * DataObject 필드가 매핑될 도메인 필드 이름 또는 경로를 지정한다.
 * ex) {@code @DataField(name = "maker.pk") private Long makerPK;}
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DataField {
    String name() default "";
}
