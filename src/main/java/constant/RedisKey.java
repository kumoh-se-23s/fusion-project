package constant;

import lombok.Getter;

@Getter
public enum RedisKey {
    ACCESS("access"),
    REFRESH("refresh"),
    OAUTH_STATE("oauth_state"),
    SOCIAL_LOGIN("social-login"),
    SOCIAL_SIGNUP("social-signup");

    private final String format;

    RedisKey(String format) {
        this.format = format;
    }
}