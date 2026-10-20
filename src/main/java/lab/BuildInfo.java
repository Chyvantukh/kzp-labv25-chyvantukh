package lab;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * Надає версію програми та номер збірки, підставлені Maven.
 */
public final class BuildInfo {
    private static final String RESOURCE = "/version.properties";

    /**
     * Забороняє створення екземплярів класу з утилітарним методом версії.
     */
    private BuildInfo() {
    }

    /**
     * Повертає версію з pom.xml разом із номером збірки.
     *
     * @return рядок у форматі (версія+збірка), наприклад 2.0.0+local або 2.0.0+42
     */
    public static String version() {
        Properties properties = new Properties();
        try (InputStream input = BuildInfo.class.getResourceAsStream(RESOURCE)) {
            if (input == null) {
                throw new IllegalStateException("Ресурс %s не знайдено".formatted(RESOURCE));
            }
            try (Reader reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
                properties.load(reader);
            }
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }

        String version = properties.getProperty("version");
        String build = properties.getProperty("build");

        if (version == null || build == null) {
            throw new IllegalStateException("У %s немає ключа version або build".formatted(RESOURCE));
        }

        return "%s+%s".formatted(version, build);
    }
}