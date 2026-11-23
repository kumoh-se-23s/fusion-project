package support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minihttp.packet.HttpResponse;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/** 서버가 돌려준 응답. 상태 코드, 헤더, body(문자열/JSON)를 꺼내 검증한다. */
public final class ApiResponse {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final String requestLine;
    private final HttpResponse raw;
    private final String body;

    ApiResponse(String requestLine, HttpResponse raw) {
        this.requestLine = requestLine;
        this.raw = raw;
        this.body = raw.body().asString(StandardCharsets.UTF_8);
    }

    public int status() {
        return this.raw.status().code();
    }

    public String body() {
        return this.body;
    }

    public Optional<String> header(String name) {
        return this.raw.header().get(name);
    }

    public HttpResponse raw() {
        return this.raw;
    }

    /** body를 JSON으로 읽는다. JSON이 아니면 실패. */
    public JsonNode json() {
        try {
            return MAPPER.readTree(this.body);
        } catch (Exception e) {
            return fail(this.requestLine + " 응답 body가 JSON이 아니다: " + this.body);
        }
    }

    /** JSON Pointer로 값 하나를 꺼낸다. 예: json("/data/id") */
    public JsonNode json(String pointer) {
        return json().at(pointer);
    }

    /** 상태 코드를 검증하고, 틀리면 body까지 보여 준다. */
    public ApiResponse assertStatus(int expected) {
        assertEquals(expected, status(), () -> this.requestLine + " 응답 상태가 다르다. body: " + this.body);
        return this;
    }

    @Override
    public String toString() {
        return this.requestLine + " -> " + status() + " " + this.body;
    }
}
