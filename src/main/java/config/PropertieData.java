package config;

/**
 * 클래스패스의 db.properties를 읽어 설정값을 제공하는 클래스
 * 실제 파일 읽기는 PropertiesFile이 담당한다.
 * 파일이 없으면 빈 설정으로 동작하고, 필수값을 조회할 때 예외가 발생한다.
 */
public final class PropertieData {
    private static final PropertiesFile FILE = new PropertiesFile("db.properties");

    private PropertieData() {
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
