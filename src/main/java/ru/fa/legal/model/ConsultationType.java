package ru.fa.legal.model;

/**
 * Перечисление типов консультаций.
 * Определяет формат проведения консультации.
 */
public enum ConsultationType {
    /** Очная консультация в офисе */
    OFFICE("В офисе"),

    /** Онлайн консультация */
    ONLINE("Онлайн"),

    /** Телефонная консультация */
    PHONE("По телефону"),

    /** Выезд к клиенту */
    HOME_VISIT("Выезд к клиенту");

    /** Русское название типа консультации */
    private final String displayName;

    /**
     * Конструктор enum.
     *
     * @param displayName русское название типа
     */
    ConsultationType(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Получить русское название типа консультации.
     *
     * @return русское название
     */
    public String getDisplayName() {
        return displayName;
    }
}
