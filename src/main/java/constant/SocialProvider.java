package constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.net.URI;

@Getter
public enum SocialProvider {
    //구글
    GOOGLE("https://accounts.google.com/o/oauth2/v2/auth","oauth2.googleapis.com/token", "openidconnect.googleapis.com/v1/userinfo"),

    //네이버
    NAVER("https://nid.naver.com/oauth2.0/authorize","nid.naver.com/oauth2.0/token", "openapi.naver.com/v1/nid/me"),

    //카카오
    KAKAO("https://kauth.kakao.com/oauth/authorize", "kauth.kakao.com/oauth/token", "kapi.kakao.com/v2/user/me");

    private final String authorizationEndpoint;
    private final URI tokenEndpoint;
    private final String userInfoEndpoint;

    SocialProvider(String authorizationEndpoint, String tokenEndpoint, String userInfoEndpoint) {
        this.authorizationEndpoint = authorizationEndpoint;
        this.tokenEndpoint = URI.create(tokenEndpoint);
        this.userInfoEndpoint = userInfoEndpoint;
    }
}
