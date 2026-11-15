package lab;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Метадані та операції, пов'язані з CSV.
 */
public final class Csv {
    private Csv() {
    }

    /**
     * Визначає заголовок стовпця CSV для поля предметної моделі.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface CsvColumn {
        /**
         * Повертає текст заголовка стовпця.
         *
         * @return заголовок стовпця CSV
         */
        String value();
    }

    /**
     * Надає представлення значення для запису у CSV.
     */
    public interface Value {
        /**
         * Повертає представлення значення у форматі CSV.
         *
         * @return представлення у форматі CSV
         */
        String toCsvValue();
    }

    /**
     * Експортує анотовані поля об'єкта у файл CSV з роздільником-крапкою з комою та кодуванням UTF-8.
     *
     * @param <T> тип запису для експорту
     */
    public static final class Exporter<T> {
        private static final String RECORD_SEPARATOR = "\r\n";

        private final List<ColumnMetadata> columns;

        /**
         * Створює експортер на основі анотованих полів типу та його суперкласів.
         *
         * @param recordType тип, анотовані поля якого визначають стовпці експорту
         * @throws IllegalArgumentException якщо немає анотованих полів екземпляра
         * @throws ReflectionException якщо неможливо отримати доступ до анотованих полів
         */
        public Exporter(Class<T> recordType) {
            Objects.requireNonNull(recordType, "recordType");
            columns = findColumns(recordType);
            if (columns.isEmpty()) {
                throw new IllegalArgumentException(
                        "Тип %s не містить анотованих CSV-полів".formatted(recordType.getName()));
            }
        }

        /**
         * Формує документ CSV із заголовком і записами.
         *
         * @param records записи для експорту
         * @return документ CSV, у якому кожен рядок завершується CRLF
         */
        public String toCsv(Iterable<? extends T> records) {
            Objects.requireNonNull(records, "records");
            StringBuilder csv = new StringBuilder();
            appendHeader(csv);
            for (T record : records) {
                appendRecord(csv, Objects.requireNonNull(record, "record"));
            }
            return csv.toString();
        }

        /**
         * Записує дані у файл CSV у кодуванні UTF-8.
         *
         * @param path шлях до файлу призначення
         * @param records записи для експорту
         * @throws IOException якщо не вдалося записати файл
         */
        public void write(Path path, Iterable<? extends T> records) throws IOException {
            Objects.requireNonNull(path, "path");
            Files.writeString(path, toCsv(records), StandardCharsets.UTF_8);
        }

        private static List<ColumnMetadata> findColumns(Class<?> recordType) {
            List<Field> fields = new ArrayList<>();
            for (Class<?> currentType = recordType;
                    currentType != null && currentType != Object.class;
                    currentType = currentType.getSuperclass()) {
                for (Field field : currentType.getDeclaredFields()) {
                    if (field.isAnnotationPresent(CsvColumn.class) && !Modifier.isStatic(field.getModifiers())) {
                        fields.add(field);
                    }
                }
            }
            fields.sort(Comparator.comparing(Field::getName)
                    .thenComparing(field -> field.getDeclaringClass().getName()));

            List<ColumnMetadata> columns = new ArrayList<>(fields.size());
            for (Field field : fields) {
                try {
                    if (!field.trySetAccessible()) {
                        throw new ReflectionException(
                                "Не вдалося отримати доступ до CSV-поля %s".formatted(field.getName()));
                    }
                } catch (SecurityException exception) {
                    throw new ReflectionException(
                            "Не вдалося отримати доступ до CSV-поля %s".formatted(field.getName()), exception);
                }
                columns.add(new ColumnMetadata(field, field.getAnnotation(CsvColumn.class).value()));
            }
            return List.copyOf(columns);
        }

        private void appendHeader(StringBuilder csv) {
            for (int index = 0; index < columns.size(); index++) {
                if (index > 0) {
                    csv.append(';');
                }
                csv.append(escape(columns.get(index).header()));
            }
            csv.append(RECORD_SEPARATOR);
        }

        private void appendRecord(StringBuilder csv, T record) {
            for (int index = 0; index < columns.size(); index++) {
                if (index > 0) {
                    csv.append(';');
                }
                csv.append(escape(readValue(columns.get(index).field(), record)));
            }
            csv.append(RECORD_SEPARATOR);
        }

        private static String readValue(Field field, Object record) {
            try {
                Object value = field.get(record);
                if (value == null) {
                    return "";
                }
                if (value instanceof Value csvValue) {
                    return Objects.requireNonNull(csvValue.toCsvValue(), "CSV value");
                }
                return value.toString();
            } catch (IllegalAccessException exception) {
                throw new ReflectionException(
                        "Не вдалося прочитати CSV-поле %s".formatted(field.getName()), exception);
            }
        }

        private static String escape(String value) {
            if (value.indexOf(';') < 0
                    && value.indexOf(',') < 0
                    && value.indexOf('"') < 0
                    && value.indexOf('\r') < 0
                    && value.indexOf('\n') < 0) {
                return value;
            }
            return "\"%s\"".formatted(value.replace("\"", "\"\""));
        }

        private record ColumnMetadata(Field field, String header) {
        }
    }
}
