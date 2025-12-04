package ru.fa.legal.model;

/**
 * Перечисление приоритетов дел.
 * Определяет срочность работы над делом.
 */
public enum Priority {
    /** Низкий приоритет */
    LOW("Низкий"),

    /** Средний приоритет */
    MEDIUM("Средний"),

    /** Высокий приоритет */
    HIGH("Высокий"),

    /** Критический приоритет */
    CRITICAL("Критический");

    /** Русское название приоритета */
    private final String displayName;

    /**
     * Конструктор enum.
     *
     * @param displayName русское название приоритета
     */
    Priority(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Получить русское название приоритета.
     *
     * @return русское название
     */
    public String getDisplayName() {
        return displayName;
    }
}
