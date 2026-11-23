package support;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 이 테스트 클래스(보통 @Nested 클래스)가 어떤 엔드포인트를 검증하는지 표시한다.
 * {@link docs.DocsEndpointCoverageTest}가 /openapi.json(docs)에 잡힌 엔드포인트와 이 표시를 비교해서
 * 테스트가 없는 엔드포인트, 또는 docs에 없는 엔드포인트를 테스트하는 클래스를 찾아낸다.
 *
 * <p>path는 docs에 나오는 모양 그대로 쓴다. 예: {@code /account/{local_account_id}}
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ApiEndpoint {
    /** HTTP 메서드. GET, POST, PATCH, PUT, DELETE */
    String method();

    /** docs에 나오는 경로 템플릿 */
    String path();
}
