package annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** 다른 엔티티를 참조하는 FK 컬럼 매핑. 참조 대상의 @PK 값이 이 컬럼에 저장된다. */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface JoinColumn {
    String name();
}
