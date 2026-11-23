package support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minihttp.core.handler.Handler;
import com.minihttp.packet.HttpRequest;
import com.minihttp.packet.HttpResponse;
import com.minihttp.packet.protocol.HttpMethod;
import com.minihttp.packet.protocol.HttpVersion;
import com.minihttp.packet.structure.DefaultHttpBody;
import com.minihttp.packet.structure.DefaultHttpHeader;
import com.minihttp.packet.structure.HttpHeader;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 클라이언트 역할. 실제 HTTP 요청과 같은 모양의 요청을 만들어 서버 핸들러(미들웨어, 라우팅, 바인딩,
 * 인증, 예외 처리, JSON 직렬화까지 전부)를 통과시키고 응답을 돌려준다.
 *
 * <pre>{@code
 * ApiResponse response = client.post("/auth/local-login")
 *         .json(Map.of("loginId", "hong", "password", "pw"))
 *         .send();
 * }</pre>
 */
public final class ApiClient {
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    private final Handler handler;

    ApiClient(Handler handler) {
        this.handler = handler;
    }

    public Request get(String path) {
        return new Request(HttpMethod.GET, path);
    }

    public Request post(String path) {
        return new Request(HttpMethod.POST, path);
    }

    public Request patch(String path) {
        return new Request(HttpMethod.PATCH, path);
    }

    public Request put(String path) {
        return new Request(HttpMethod.PUT, path);
    }

    public Request delete(String path) {
        return new Request(HttpMethod.DELETE, path);
    }

    public Request request(String method, String path) {
        return new Request(HttpMethod.valueOf(method.toUpperCase()), path);
    }

    public final class Request {
        private final HttpMethod method;
        private final String path;
        private final Map<String, List<String>> query = new LinkedHashMap<>();
        private final HttpHeader header = new DefaultHttpHeader();
        private byte[] body = new byte[0];

        private Request(HttpMethod method, String path) {
            this.method = method;
            this.path = path;
            this.header.set("Host", "localhost");
        }

        /** 쿼리 파라미터. 같은 키로 여러 번 부르면 값이 여러 개가 된다 (List 파라미터). */
        public Request query(String key, Object value) {
            this.query.computeIfAbsent(key, k -> new ArrayList<>()).add(String.valueOf(value));
            return this;
        }

        public Request header(String name, String value) {
            this.header.set(name, value);
            return this;
        }

        /** Authorization: Bearer {token} */
        public Request bearer(String token) {
            return header("Authorization", "Bearer " + token);
        }

        /** 객체를 JSON으로 바꿔 body에 넣는다. Map이나 DTO 모두 가능. */
        public Request json(Object value) {
            try {
                return rawJson(MAPPER.writeValueAsString(value));
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException(e);
            }
        }

        /** JSON 문자열을 그대로 body에 넣는다. 깨진 JSON 테스트에 쓴다. */
        public Request rawJson(String json) {
            this.body = json.getBytes(StandardCharsets.UTF_8);
            this.header.set("Content-Type", "application/json");
            return this;
        }

        public ApiResponse send() {
            this.header.set("Content-Length", String.valueOf(this.body.length));
            HttpRequest request = new HttpRequest(
                    HttpVersion.HTTP_1_1,
                    this.header,
                    new DefaultHttpBody(this.body),
                    this.method,
                    this.path,
                    this.query,
                    Map.of()
            );
            try {
                HttpResponse response = handler.handle(request);
                return new ApiResponse(this.method + " " + this.path, response);
            } catch (Exception e) {
                throw new AssertionError("핸들러가 응답 대신 예외를 던졌다: " + this.method + " " + this.path, e);
            }
        }
    }
}
