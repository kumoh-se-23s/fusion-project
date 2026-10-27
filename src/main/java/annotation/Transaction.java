package annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 서비스 메서드를 요청 명령어로 등록. RequestHandler.registerAll()이 시작 시 읽어서 등록한다.
 * 메서드는 public이고, Request를 상속한 파라미터 하나와 Response를 상속한 반환 타입을 가져야 한다.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Transaction {
    //요청 이름 (예: "category.create")
    String name();

    //true면 읽기 전용 트랜잭션. 쓰기 쿼리 실행 시 DB에서 예외 발생
    boolean readOnly() default false;
}
