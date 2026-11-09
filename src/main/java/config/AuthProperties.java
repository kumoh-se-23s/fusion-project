package config;

/**
 * 클래스패스의 auth.properties를 읽어 인증 관련 설정값을 제공하는 클래스
 * (소셜 로그인 client-id / client-secret / redirect-uri, JWT 비밀키 등)
 * 실제 파일 읽기는 PropertiesFile이 담당한다.
 * 키 목록은 src/main/resources/auth.properties.example 참고
 */
public final class AuthProperties {
    private static final PropertiesFile FILE = new PropertiesFile("auth.properties");

    private AuthProperties() {
    }

    //필수 설정값
    public static String get(String key) {
        return FILE.get(key);
    }

    public static String get(String key, String defaultValue) {
        return FILE.get(key, defaultValue);
    }

    public static int getInt(String key, int defaultValue) {
        return FILE.getInt(key, defaultValue);
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        return FILE.getBoolean(key, defaultValue);
    }
}
