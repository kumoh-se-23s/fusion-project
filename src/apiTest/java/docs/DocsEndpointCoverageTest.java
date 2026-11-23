package docs;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import support.ApiEndpoint;
import support.ApiResponse;
import support.TestServer;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * docs(/openapi.json)에 잡힌 엔드포인트와 테스트 코드의 {@link ApiEndpoint} 표시를 비교한다.
 * 엔드포인트를 추가했는데 테스트를 안 만들었거나, 경로가 바뀌었는데 테스트가 옛 경로를 보고 있으면 실패한다.
 */
@DisplayName("docs 엔드포인트 커버리지")
class DocsEndpointCoverageTest {
    private static final Set<String> HTTP_METHODS = Set.of("get", "post", "put", "patch", "delete");

    @Test
    @DisplayName("docs 페이지와 스펙이 열린다")
    void docsAreServed() {
        TestServer.get().client().get("/docs").send().assertStatus(200);
        TestServer.get().client().get("/openapi.json").send().assertStatus(200);
    }

    @Test
    @DisplayName("docs의 모든 엔드포인트에 테스트가 있고, 테스트가 가리키는 엔드포인트는 모두 docs에 있다")
    void everyDocumentedEndpointIsTested() throws Exception {
        Set<String> documented = documentedEndpoints();
        Map<String, String> tested = testedEndpoints();

        Set<String> untested = new TreeSet<>(documented);
        untested.removeAll(tested.keySet());

        Map<String, String> unknown = new TreeMap<>(tested);
        unknown.keySet().removeAll(documented);

        assertTrue(untested.isEmpty() && unknown.isEmpty(), () ->
                "테스트가 없는 엔드포인트:\n  " + String.join("\n  ", untested)
                        + "\ndocs에 없는 엔드포인트를 가리키는 테스트:\n  "
                        + String.join("\n  ", unknown.entrySet().stream()
                                                     .map(e -> e.getKey() + "  (" + e.getValue() + ")")
                                                     .toList()));
    }

    static Set<String> documentedEndpoints() {
        ApiResponse spec = TestServer.get().client().get("/openapi.json").send().assertStatus(200);
        Set<String> endpoints = new TreeSet<>();
        JsonNode paths = spec.json("/paths");
        for (Iterator<Map.Entry<String, JsonNode>> it = paths.fields(); it.hasNext(); ) {
            Map.Entry<String, JsonNode> path = it.next();
            for (Iterator<String> methods = path.getValue().fieldNames(); methods.hasNext(); ) {
                String method = methods.next();
                if (HTTP_METHODS.contains(method)) {
                    endpoints.add(key(method, path.getKey()));
                }
            }
        }
        return endpoints;
    }

    /** 테스트 클래스 출력 폴더를 훑어서 @ApiEndpoint가 붙은 클래스를 모은다. 값은 클래스 이름. */
    static Map<String, String> testedEndpoints() throws IOException, URISyntaxException, ClassNotFoundException {
        Path root = Path.of(DocsEndpointCoverageTest.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        Map<String, String> endpoints = new TreeMap<>();
        try (Stream<Path> files = Files.walk(root)) {
            for (Path file : files.filter(f -> f.toString().endsWith(".class")).toList()) {
                String className = root.relativize(file).toString()
                                       .replace(".class", "")
                                       .replace(file.getFileSystem().getSeparator(), ".");
                Class<?> type = Class.forName(className, false, DocsEndpointCoverageTest.class.getClassLoader());
                ApiEndpoint endpoint = type.getAnnotation(ApiEndpoint.class);
                if (endpoint != null) {
                    endpoints.merge(key(endpoint.method(), endpoint.path()), type.getName(), (a, b) -> a + ", " + b);
                }
            }
        }
        return endpoints;
    }

    private static String key(String method, String path) {
        return method.toUpperCase(Locale.ROOT) + " " + path;
    }
}
