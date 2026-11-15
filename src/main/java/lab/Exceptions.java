package lab;

/**
 * Базовий неперевірюваний виняток для помилок предметної області.
 */
class DomainException extends RuntimeException {
    public DomainException(String message) {
        super(message);
    }

    public DomainException(String message, Throwable cause) {
        super(message, cause);
    }

    public DomainException(Throwable cause) {
        super(cause);
    }
}

/**
 * Вказує на порушення інваріантів значення предметної моделі.
 */
class ModelValidationException extends DomainException {
    ModelValidationException(String message) {
        super(message);
    }

    ModelValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    ModelValidationException(Throwable cause) {
        super(cause);
    }
}

/**
 * Вказує на те, що файл або потік не відповідає очікуваному формату.
 */
class FileFormatException extends DomainException {
    FileFormatException(String message) {
        super(message);
    }

    FileFormatException(String message, Throwable cause) {
        super(message, cause);
    }

    FileFormatException(Throwable cause) {
        super(cause);
    }
}

/**
 * Вказує на помилку доступу до файлу або файлової системи, а не на помилковий формат даних.
 */
class FileAccessException extends DomainException {
    FileAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    FileAccessException(Throwable cause) {
        super(cause);
    }
}

/**
 * Вказує на помилку рефлексивного доступу або створення екземпляра типу предметної області.
 */
class ReflectionException extends DomainException {
    ReflectionException(String message) {
        super(message);
    }

    ReflectionException(String message, Throwable cause) {
        super(message, cause);
    }

    ReflectionException(Throwable cause) {
        super(cause);
    }
}
