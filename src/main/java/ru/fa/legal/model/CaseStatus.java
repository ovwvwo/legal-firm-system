package ru.fa.legal.model;

/**
 * Перечисление статусов дел.
 * Определяет текущее состояние работы над делом.
 */
public enum CaseStatus {
    /** Новое дело, только открыто */
    NEW("Новое"),

    /** Дело в работе */
    IN_PROGRESS("В работе"),

    /** Ожидание судебного заседания */
    AWAITING_HEARING("Ожидает заседания"),

    /** Приостановлено */
    SUSPENDED("Приостановлено"),

    /** Дело выиграно */
    WON("Выиграно"),

    /** Дело проиграно */
    LOST("Проиграно"),

    /** Мировое соглашение */
    SETTLED("Мировое соглашение"),

    /** Закрыто */
    CLOSED("Закрыто");

    /** Русское название статуса */
    private final String displayName;

    /**
     * Конструктор enum.
     *
     * @param displayName русское название статуса
     */
    CaseStatus(String displayName) {
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
