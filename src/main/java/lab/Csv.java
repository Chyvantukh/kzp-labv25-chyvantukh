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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

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

    private record ColumnMetadata(Field field, String header) {
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

        List<String> headers = columns.stream().map(ColumnMetadata::header).toList();
        if (headers.stream().distinct().count() != headers.size()) {
            throw new IllegalArgumentException(
                    "Тип %s має повторювані заголовки CSV".formatted(recordType.getName()));
        }
        return List.copyOf(columns);
    }

    /** Імпортує записи з документа CSV, використовуючи анотовані заголовки. */
    public static final class Importer<T> {
        private final List<ColumnMetadata> columns;
        private final Function<Map<String, String>, ? extends T> recordFactory;
        private final char delimiter;

        /**
         * Створює імпортер, який зіставляє значення кожного рядка з анотованими заголовками.
         *
         * @param recordType тип, анотовані поля якого визначають очікуваний заголовок CSV
         * @param recordFactory фабричний метод, що створює запис із пар «заголовок — значення»
         */
        public Importer(Class<T> recordType, Function<Map<String, String>, ? extends T> recordFactory) {
            this(recordType, ',', recordFactory);
        }

        /**
         * Створює імпортер із заданим роздільником полів.
         *
         * @param recordType тип, анотовані поля якого визначають очікуваний заголовок CSV
         * @param delimiter роздільник полів CSV
         * @param recordFactory фабричний метод, що створює запис із пар «заголовок — значення»
         */
        public Importer(
                Class<T> recordType, char delimiter, Function<Map<String, String>, ? extends T> recordFactory) {
            Objects.requireNonNull(recordType, "recordType");
            columns = findColumns(recordType);
            if (columns.isEmpty()) {
                throw new IllegalArgumentException(
                        "Тип %s не містить анотованих CSV-полів".formatted(recordType.getName()));
            }
            if (delimiter == '"' || delimiter == '\r' || delimiter == '\n') {
                throw new IllegalArgumentException("Некоректний роздільник CSV");
            }
            this.delimiter = delimiter;
            this.recordFactory = Objects.requireNonNull(recordFactory, "recordFactory");
        }

        /**
         * Зчитує та розбирає документ CSV у кодуванні UTF-8.
         *
         * @param path шлях до документа CSV
         * @return незмінний список записів у порядку їх появи в документі
         * @throws FileAccessException якщо не вдалося прочитати файл
         * @throws FileFormatException якщо заголовок, кількість полів у рядку або лапкування CSV некоректні
         */
        public List<T> read(Path path) {
            return read(path, (recordNumber, exception) -> {
                if (exception instanceof FileFormatException formatException) {
                    throw new FileFormatException(
                            "Рядок %d: %s".formatted(recordNumber + 1, formatException.getMessage()),
                            formatException);
                }
                throw exception;
            });
        }

        /**
         * Reads records, sending malformed rows and model-validation failures to a handler.
         *
         * @param path path to the CSV document
         * @param rowErrorHandler receives the 1-based data-record number and its error
         * @return immutable valid records in document order
         * @throws FileAccessException if the file cannot be read
         * @throws FileFormatException if the header or CSV quoting is invalid
         */
        public List<T> read(Path path, BiConsumer<Integer, RuntimeException> rowErrorHandler) {
            Objects.requireNonNull(path, "path");
            Objects.requireNonNull(rowErrorHandler, "rowErrorHandler");
            try {
                String document = Files.readString(path, StandardCharsets.UTF_8);
                List<List<String>> rows = parseRows(document, delimiter);
                if (rows.isEmpty()) {
                    throw new FileFormatException("CSV-файл не містить заголовка");
                }

                List<String> expectedHeaders = columns.stream().map(ColumnMetadata::header).toList();
                List<String> actualHeaders = rows.getFirst();
                if (!expectedHeaders.equals(actualHeaders)) {
                    throw new FileFormatException("Заголовок CSV не відповідає очікуваному: %s"
                            .formatted(String.join(String.valueOf(delimiter), expectedHeaders)));
                }

                List<T> records = new ArrayList<>(rows.size() - 1);
                for (int rowIndex = 1; rowIndex < rows.size(); rowIndex++) {
                    List<String> values = rows.get(rowIndex);
                    if (values.size() != columns.size()) {
                        String message = values.size() > columns.size()
                                ? "забагато полiв (очiкувалося %d, знайдено %d)"
                                        .formatted(columns.size(), values.size())
                                : "очікувалося %d полів, знайдено %d".formatted(columns.size(), values.size());
                        rowErrorHandler.accept(rowIndex, new FileFormatException(
                                message));
                        continue;
                    }

                    Map<String, String> row = new LinkedHashMap<>();
                    for (int columnIndex = 0; columnIndex < columns.size(); columnIndex++) {
                        row.put(columns.get(columnIndex).header(), values.get(columnIndex));
                    }
                    try {
                        T record = recordFactory.apply(Map.copyOf(row));
                        records.add(Objects.requireNonNull(record, "recordFactory result"));
                    } catch (IllegalArgumentException exception) {
                        rowErrorHandler.accept(rowIndex, exception);
                    }
                }
                return List.copyOf(records);
            } catch (IOException exception) {
                throw new FileAccessException(
                        "Не вдалося прочитати CSV-файл %s".formatted(path), exception);
            }
        }

        /**
         * Reads a document without a header row.
         *
         * @param path path to the CSV document
         * @param rowErrorHandler receives the 1-based line number and row error
         * @return immutable valid records in document order
         * @throws FileAccessException if the file cannot be read
         * @throws FileFormatException if the CSV quoting is invalid
         */
        public List<T> readHeaderless(Path path, BiConsumer<Integer, RuntimeException> rowErrorHandler) {
            Objects.requireNonNull(path, "path");
            Objects.requireNonNull(rowErrorHandler, "rowErrorHandler");
            try {
                String document = Files.readString(path, StandardCharsets.UTF_8);
                List<List<String>> rows = parseRows(document, delimiter);
                List<T> records = new ArrayList<>(rows.size());

                for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
                    List<String> values = rows.get(rowIndex);
                    if (values.size() != columns.size()) {
                        String message = values.size() > columns.size()
                                ? "забагато полiв (очiкувалося %d, знайдено %d)"
                                        .formatted(columns.size(), values.size())
                                : "очікувалося %d полів, знайдено %d"
                                        .formatted(columns.size(), values.size());
                        rowErrorHandler.accept(rowIndex, new FileFormatException(message));
                        continue;
                    }

                    Map<String, String> row = new LinkedHashMap<>();
                    for (int columnIndex = 0; columnIndex < columns.size(); columnIndex++) {
                        row.put(columns.get(columnIndex).header(), values.get(columnIndex));
                    }
                    try {
                        T record = recordFactory.apply(Map.copyOf(row));
                        records.add(Objects.requireNonNull(record, "recordFactory result"));
                    } catch (IllegalArgumentException exception) {
                        rowErrorHandler.accept(rowIndex, exception);
                    }
                }
                return List.copyOf(records);
            } catch (IOException exception) {
                throw new FileAccessException(
                        "Не вдалося прочитати CSV-файл %s".formatted(path), exception);
            }
        }
    }

    /**
     * Експортує анотовані поля об'єкта у файл CSV з роздільником-комою та кодуванням UTF-8.
     *
     * @param <T> тип запису для експорту
     */
    public static final class Exporter<T> {
        private static final String RECORD_SEPARATOR = "\r\n";
        private static final char FIELD_SEPARATOR = ',';

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
         * Формує CSV з незмінного знімка репозиторію.
         *
         * @param repository репозиторій записів
         * @return документ CSV
         */
        public String toCsv(Repository<? extends T> repository) {
            Objects.requireNonNull(repository, "repository");
            return toCsv(repository.all());
        }

        /**
         * Записує дані у файл CSV у кодуванні UTF-8.
         *
         * @param path шлях до файлу призначення
         * @param records записи для експорту
         * @throws FileAccessException якщо не вдалося записати файл
         */
        public void write(Path path, Iterable<? extends T> records) {
            Objects.requireNonNull(path, "path");
            try {
                Files.writeString(path, toCsv(records), StandardCharsets.UTF_8);
            } catch (IOException exception) {
                throw new FileAccessException(
                        "Не вдалося записати CSV-файл %s".formatted(path), exception);
            }
        }

        /**
         * Записує незмінний знімок репозиторію у файл CSV у кодуванні UTF-8.
         *
         * @param path шлях до файлу призначення
         * @param repository репозиторій записів
         * @throws FileAccessException якщо не вдалося записати файл
         */
        public void write(Path path, Repository<? extends T> repository) {
            Objects.requireNonNull(repository, "repository");
            write(path, repository.all());
        }

        private void appendHeader(StringBuilder csv) {
            for (int index = 0; index < columns.size(); index++) {
                if (index > 0) {
                    csv.append(FIELD_SEPARATOR);
                }
                csv.append(escape(columns.get(index).header()));
            }
            csv.append(RECORD_SEPARATOR);
        }

        private void appendRecord(StringBuilder csv, T record) {
            for (int index = 0; index < columns.size(); index++) {
                if (index > 0) {
                    csv.append(FIELD_SEPARATOR);
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
            if (value.indexOf(FIELD_SEPARATOR) < 0
                    && value.indexOf('"') < 0
                    && value.indexOf('\r') < 0
                    && value.indexOf('\n') < 0) {
                return value;
            }
            return "\"%s\"".formatted(value.replace("\"", "\"\""));
        }
    }

    private static List<List<String>> parseRows(String document, char delimiter) {
        List<List<String>> rows = new ArrayList<>();
        List<String> row = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean inQuotes = false;
        boolean afterQuote = false;

        for (int index = 0; index < document.length(); index++) {
            char character = document.charAt(index);
            if (inQuotes) {
                if (character == '"') {
                    if (index + 1 < document.length() && document.charAt(index + 1) == '"') {
                        field.append('"');
                        index++;
                    } else {
                        inQuotes = false;
                        afterQuote = true;
                    }
                } else {
                    field.append(character);
                }
                continue;
            }

            if (afterQuote) {
                if (character == delimiter) {
                    row.add(field.toString());
                    field.setLength(0);
                    afterQuote = false;
                } else if (character == '\r' || character == '\n') {
                    row.add(field.toString());
                    rows.add(List.copyOf(row));
                    row.clear();
                    field.setLength(0);
                    afterQuote = false;
                    if (character == '\r' && index + 1 < document.length()
                            && document.charAt(index + 1) == '\n') {
                        index++;
                    }
                } else {
                    throw new FileFormatException(
                            "Некоректний символ після закривальних лапок у CSV");
                }
                continue;
            }

            if (character == '"') {
                if (field.length() > 0) {
                    throw new FileFormatException("Лапки всередині незакритого поля CSV");
                }
                inQuotes = true;
            } else if (character == delimiter) {
                row.add(field.toString());
                field.setLength(0);
            } else if (character == '\r' || character == '\n') {
                row.add(field.toString());
                rows.add(List.copyOf(row));
                row.clear();
                field.setLength(0);
                if (character == '\r' && index + 1 < document.length()
                        && document.charAt(index + 1) == '\n') {
                    index++;
                }
            } else {
                field.append(character);
            }
        }

        if (inQuotes) {
            throw new FileFormatException("Незакрите лапковане поле CSV");
        }
        if (afterQuote || field.length() > 0 || !row.isEmpty()) {
            row.add(field.toString());
            rows.add(List.copyOf(row));
        }
        return List.copyOf(rows);
    }
}
