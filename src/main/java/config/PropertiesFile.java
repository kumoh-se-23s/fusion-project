package config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 클래스패스(src/main/resources)에 있는 .properties 파일 하나를 읽어 설정값을 제공하는 공통 클래스
 * 파일이 없으면 빈 설정으로 동작하고, 필수값을 조회할 때 예외가 발생한다.
 * 파일마다 이 클래스를 하나씩 만들어 사용한다. (예: db.properties, auth.properties)
 */
public final class PropertiesFile {
    private final String fileName;
    private final Properties properties;

    public PropertiesFile(String fileName) {
        this.fileName = fileName;
        this.properties = load(fileName);
    }

    private static Properties load(String fileName) {
        Properties properties = new Properties();
        try (InputStream in = PropertiesFile.class.getClassLoader().getResourceAsStream(fileName)) {
            if (in != null) {
                properties.load(in);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load " + fileName, e);
        }
        return properties;
    }

    //필수 설정값
    public String get(String key) {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing property '" + key + "' in " + fileName);
        }
        return value.trim();
    }

    public String get(String key, String defaultValue) {
        String value = properties.getProperty(key);
        return (value == null || value.isBlank()) ? defaultValue : value.trim();
    }

    public int getInt(String key, int defaultValue) {
        return Integer.parseInt(get(key, String.valueOf(defaultValue)));
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return Boolean.parseBoolean(get(key, String.valueOf(defaultValue)));
    }
}
