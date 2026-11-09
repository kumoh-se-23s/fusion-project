package constant;

import lombok.Getter;

import java.net.URI;

@Getter
public enum SocialProvider {
    //구글
    GOOGLE("https://accounts.google.com/o/oauth2/v2/auth",
            "https://oauth2.googleapis.com/token",
            "https://openidconnect.googleapis.com/v1/userinfo"),

    //네이버
    NAVER("https://nid.naver.com/oauth2.0/authorize",
            "https://nid.naver.com/oauth2.0/token",
            "https://openapi.naver.com/v1/nid/me") ,

    //카카오
    KAKAO("https://kauth.kakao.com/oauth/authorize",
            "https://kauth.kakao.com/oauth/token",
            "https://kapi.kakao.com/v2/user/me");

    private final String authorizationEndpoint;
    private final URI tokenEndpoint;
    private final URI userInfoEndpoint;

    SocialProvider(String authorizationEndpoint, String tokenEndpoint, String userInfoEndpoint) {
        this.authorizationEndpoint = authorizationEndpoint;
        this.tokenEndpoint = URI.create(tokenEndpoint);
        this.userInfoEndpoint = URI.create(userInfoEndpoint);
    }
}m