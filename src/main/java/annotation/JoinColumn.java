package annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** 다른 엔티티를 참조하는 FK 컬럼 매핑. 필드 타입은 연관 엔티티이며, 참조 대상의 @PK 값이 이 컬럼에 저장된다. */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface JoinColumn {
    String name();

    boolean nullable() default true;

    //컬럼 하나만으로 유일
    boolean unique() default false;

    //같은 그룹 이름을 가진 컬럼(@Column, @JoinColumn)끼리 묶어서 복합 UNIQUE
    String uniqueGroup() default "";

    //참조 대상(부모)이 삭제될 때의 동작
    OnDelete onDelete() default OnDelete.NONE;
}
