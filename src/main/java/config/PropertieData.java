package config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 클래스패스의 db.properties를 읽어 설정값을 제공하는 클래스
 * 파일이 없으면 빈 설정으로 동작하고, 필수값을 조회할 때 예외가 발생한다.
 */
public final class PropertieData {
    private static final String FILE_NAME = "db.properties";
    private static final Properties PROPERTIES = load();

    private PropertieData() {
    }

    private static Properties load() {
        Properties properties = new Properties();
        try (InputStream in = PropertieData.class.getClassLoader().getResourceAsStream(FILE_NAME)) {
            if (in != null) {
                properties.load(in);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load " + FILE_NAME, e);
        }
        return properties;
    }

    //필수 설정값
    public static String get(String key) {
        String value = PROPERTIES.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing property '" + key + "' in " + FILE_NAME);
        }
        return value.trim();
    }

    public static String get(String key, String defaultValue) {
        String value = PROPERTIES.getProperty(key);
        return (value == null || value.isBlank()) ? defaultValue : value.trim();
    }

    public static int getInt(String key, int defaultValue) {
        return Integer.parseInt(get(key, String.valueOf(defaultValue)));
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        return Boolean.parseBoolean(get(key, String.valueOf(defaultValue)));
    }
}
