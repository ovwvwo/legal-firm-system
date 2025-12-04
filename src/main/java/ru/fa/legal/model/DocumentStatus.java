package ru.fa.legal.model;

/**
 * Перечисление статусов документов.
 * Определяет состояние документа в процессе работы.
 */
public enum DocumentStatus {
    /** Черновик */
    DRAFT("Черновик"),

    /** На рассмотрении */
    UNDER_REVIEW("На рассмотрении"),

    /** Утвержден */
    APPROVED("Утвержден"),

    /** Подписан */
    SIGNED("Подписан"),

    /** Отправлен */
    SENT("Отправлен"),

    /** Получен */
    RECEIVED("Получен"),

    /** Архивирован */
    ARCHIVED("Архивирован");

    /** Русское название статуса */
    private final String displayName;

    /**
     * Конструктор enum.
     *
     * @param displayName русское название статуса
     */
    DocumentStatus(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Получить русское название статуса.
     *
     * @return русское название
     */
    public String getDisplayName() {
        return displayName;
    }
}
