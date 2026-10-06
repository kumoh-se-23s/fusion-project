package annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DataField {
    Class<?> to() default void.class;
    String name() default "";
}
