package lab;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Узагальнений контракт репозиторію для незмінних знімків і запитів за предикатом.
 *
 * @param <T> тип збережених записів
 */
public interface Repository<T> {
    /**
     * Зберігає запис у репозиторії.
     *
     * @param item запис для додавання; не може бути {@code null}
     */
    void add(T item);

    /**
     * Повертає незмінний знімок поточних записів.
     *
     * @return збережені записи у порядку додавання
     */
    List<T> all();

    /**
     * Знаходить усі записи, що відповідають предикату.
     *
     * @param predicate умова відбору; не може бути {@code null}
     * @return записи, що відповідають умові, у порядку додавання
     */
    List<T> find(Predicate<? super T> predicate);
}

/**
 * Репозиторій у пам'яті, що зберігає записи в упорядкованому списку.
 *
 * @param <T> тип збережених записів
 */
class InMemoryRepository<T> implements Repository<T> {
    private final List<T> records = new ArrayList<>();

    @Override
    public void add(T item) {
        records.add(Objects.requireNonNull(item, "item"));
    }

    @Override
    public List<T> all() {
        return List.copyOf(records);
    }

    @Override
    public List<T> find(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate");
        List<T> matches = new ArrayList<>();
        for (T item : records) {
            if (predicate.test(item)) {
                matches.add(item);
            }
        }
        return List.copyOf(matches);
    }
}
