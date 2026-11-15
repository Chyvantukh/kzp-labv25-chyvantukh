package lab;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

class RepositoryTest {

    @Test
    void repository_shouldPreserveInsertionOrder() {
        InMemoryRepository<String> repository = new InMemoryRepository<>();

        repository.add("first");
        repository.add("second");
        repository.add("third");

        assertEquals(List.of("first", "second", "third"), repository.all());
    }

    @Test
    void repository_shouldReturnMatchingRowsInInsertionOrder() {
        InMemoryRepository<String> repository = new InMemoryRepository<>();
        repository.add("alpha");
        repository.add("beta");
        repository.add("gamma");
        repository.add("beta-two");

        assertEquals(List.of("beta", "beta-two"), repository.find(value -> value.startsWith("beta")));
        assertEquals(List.of(), repository.find(value -> false));
    }

    @Test
    void repository_shouldRejectNulls() {
        InMemoryRepository<String> repository = new InMemoryRepository<>();

        assertThrows(NullPointerException.class, () -> repository.add(null));
        assertThrows(NullPointerException.class, () -> repository.find(null));
    }

    @Test
    void repository_shouldReturnImmutableSnapshots() {
        InMemoryRepository<String> repository = new InMemoryRepository<>();
        repository.add("first");

        List<String> snapshot = repository.all();
        List<String> matches = repository.find(value -> true);

        assertThrows(UnsupportedOperationException.class, () -> snapshot.add("second"));
        assertThrows(UnsupportedOperationException.class, () -> matches.add("third"));
    }

    @Test
    void repository_snapshot_shouldStayStableAfterAdd() {
        InMemoryRepository<String> repository = new InMemoryRepository<>();
        repository.add("first");

        List<String> snapshot = repository.all();
        repository.add("second");

        assertEquals(List.of("first"), snapshot);
        assertEquals(List.of("first", "second"), repository.all());
    }
}
