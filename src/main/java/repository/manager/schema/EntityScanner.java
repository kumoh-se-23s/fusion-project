package repository.manager.schema;

import annotation.Table;
import exception.AnnotationMissingException;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

/**
 * 패키지 안에서 @Table이 붙은 엔티티 클래스를 찾는 클래스
 * IDE/Gradle 실행(디렉터리)과 jar 실행 둘 다 지원한다. 하위 패키지는 탐색하지 않는다.
 */
final class EntityScanner {

    private EntityScanner() {
    }

    static List<Class<?>> scan(String basePackage) {
        String path = basePackage.replace('.', '/');
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        List<String> classNames = new ArrayList<>();
        try {
            Enumeration<URL> urls = loader.getResources(path);
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                if ("jar".equals(url.getProtocol())) {
                    classNames.addAll(fromJar(url, path));
                } else {
                    classNames.addAll(fromDirectory(url, basePackage));
                }
            }
        } catch (IOException | URISyntaxException e) {
            throw new AnnotationMissingException("Failed to scan package " + basePackage, e);
        }

        List<Class<?>> entities = new ArrayList<>();
        for (String name : classNames) {
            Class<?> type = load(name, loader);
            if (type.isAnnotationPresent(Table.class)) {
                entities.add(type);
            }
        }
        entities.sort(Comparator.comparing(Class::getName));
        return entities;
    }

    private static List<String> fromDirectory(URL url, String basePackage) throws IOException, URISyntaxException {
        try (Stream<Path> files = Files.list(Path.of(url.toURI()))) {
            return files.map(p -> p.getFileName().toString())
                    .filter(EntityScanner::isTopLevelClass)
                    .map(file -> basePackage + "." + file.substring(0, file.length() - ".class".length()))
                    .toList();
        }
    }

    private static List<String> fromJar(URL url, String path) throws IOException {
        List<String> names = new ArrayList<>();
        JarURLConnection connection = (JarURLConnection) url.openConnection();
        try (JarFile jar = connection.getJarFile()) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                String entry = entries.nextElement().getName();
                String file = entry.substring(entry.lastIndexOf('/') + 1);
                boolean directChild = entry.startsWith(path + "/") && entry.indexOf('/', path.length() + 1) < 0;
                if (directChild && isTopLevelClass(file)) {
                    names.add(entry.substring(0, entry.length() - ".class".length()).replace('/', '.'));
                }
            }
        }
        return names;
    }

    private static boolean isTopLevelClass(String fileName) {
        return fileName.endsWith(".class") && !fileName.contains("$");
    }

    private static Class<?> load(String name, ClassLoader loader) {
        try {
            return Class.forName(name, false, loader);
        } catch (ClassNotFoundException e) {
            throw new AnnotationMissingException("Cannot load " + name, e);
        }
    }
}
