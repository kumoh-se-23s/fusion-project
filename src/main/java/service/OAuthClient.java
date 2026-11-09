package service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import constant.SocialProvider;
import exception.InvalidException;
import exception.NotFoundException;
import model.dto.SocialUserInfo;
import util.RedisUtil;

import java.io.IOException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

public class OAuthClient {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final RedisUtil redisUtil;
    private final HttpClient httpClient;
    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;

    public OAuthClient(RedisUtil redisUtil, HttpClient httpClient, String clientId, String clientSecret, String redirectUri) {
        this.redisUtil = redisUtil;
        this.httpClient = httpClient;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
    }

    public String getAuthorizationUrl(SocialProvider socialProvider) {
        String state = UUID.randomUUID().toString();
        redisUtil.save("oauth_state : " + state, state, 300);
        return buildAuthUrl(socialProvider, state);
    }

    public boolean verifyState(String state) {
        String value = redisUtil.getValue("oauth_state : " + state).orElseThrow(NotFoundException::new);
        return true;
    }

    public SocialUserInfo fetchUserInfo(SocialProvider socialProvider, String exchangedCdoe) {
        String accessToken = extractString(exchangedCdoe, "access_token");
        JsonNode userInfoJsonNode = requestUserInfo(socialProvider, accessToken);

        String email;
        String name;
        SocialUserInfo userInfo = switch (socialProvider) {
            case GOOGLE -> SocialUserInfo.builder()
                    .email(userInfoJsonNode.get("email").asText())
                    .name(userInfoJsonNode.get("name").asText())
                    .build();

            case KAKAO -> SocialUserInfo.builder()
                    .email(userInfoJsonNode.get("kakao_account").get("email").asText())
                    .name(userInfoJsonNode.get("kakao_account").get("profile").get("nickname").asText())
                    .build();

            case NAVER -> SocialUserInfo.builder()
                    .email(userInfoJsonNode.get("response").get("email").asText())
                    .name(userInfoJsonNode.get("response").get("name").asText())
                    .build();
        };

        return userInfo;
    }

    private String buildAuthUrl(SocialProvider socialProvider, String state) {
        String responseType = "code";
        String scope = "openid%20email%20profile";

        return socialProvider.getAuthorizationEndpoint() + "?client_id=" + clientId + "&redirect_uri=" + redirectAddress + "&response_type=" + responseType + "&scope=" + scope + "&state=" + state;
    }

    private String extractString(String json, String key) {
        JsonNode root;
        try {
            root = OBJECT_MAPPER.readTree(json);
        } catch (JsonProcessingException e) {
            throw new InvalidException();
        }

        return root.get(key).asText();
    }

    public String exchangeCode(SocialProvider socialProvider, String code) {
        String body = buildTokenRequestsBody(socialProvider, code);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(socialProvider.getTokenEndpoint())
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        return sendRequest(request);
    }

    public String getAccessToken(String json) {
        try {
            JsonNode root = OBJECT_MAPPER.readTree(json);

            return root.get("access_token").asText();

        } catch (JsonProcessingException e) {
            throw new InvalidException();
        }
    }

    private JsonNode requestUserInfo(SocialProvider socialProvider, String accessToken) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(socialProvider.getUserInfoEndpoint())
                .timeout(Duration.ofSeconds(10))
                .header("Authorization", "Bearer " + accessToken)
                .header("Accept", "application/json")
                .GET()
                .build();

        String jsonUserInfo = sendRequest(request);
        JsonNode userInfo;
        try {
            userInfo = OBJECT_MAPPER.readTree(jsonUserInfo);
        } catch (JsonProcessingException e) {
            throw new InvalidException();
        }

        return userInfo;
    }

    private String sendRequest(HttpRequest request) {
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return response.body();
            }

            //수정 필요
            throw new InvalidException();
        } catch (IOException | InterruptedException e) {
            throw new InvalidException();
        }
    }

    private String buildTokenRequestsBody(SocialProvider socialProvider, String code) {
        Map<String, String> params = new LinkedHashMap<>();

        params.put("code", code);
        params.put("client_id", clientId);
        params.put("client_secret", clientSecret);
        params.put("redirect_uri", redirectUri);
        params.put("grant_type", "authorization_code");


        if (socialProvider.equals(SocialProvider.GOOGLE)) {

        } else if (socialProvider.equals(SocialProvider.NAVER)) {

        } else if (socialProvider.equals(SocialProvider.KAKAO)) {

        }

        return encodedForm(params);
    }

    private Optional<String> encode(String value) {
        if (value == null) {
            return Optional.empty();
        }

        return Optional.of(URLEncoder.encode(value, StandardCharsets.UTF_8));
    }

    private String encodedForm(Map<String, String> params) {
        return params.entrySet().stream()
                .map(e -> encode(e.getKey()).orElseThrow(InvalidException::new) + "=" + encode(e.getValue()).orElseThrow(InvalidException::new))
                .collect(Collectors.joining("&"));
    }
}
