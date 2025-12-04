package ru.fa.legal.model;

/**
 * Перечисление статусов консультаций.
 * Определяет текущее состояние записи на консультацию.
 */
public enum ConsultationStatus {
    /** Запланирована */
    SCHEDULED("Запланирована"),

    /** Подтверждена */
    CONFIRMED("Подтверждена"),

    /** В процессе */
    IN_PROGRESS("В процессе"),

    /** Завершена */
    COMPLETED("Завершена"),

    /** Отменена клиентом */
    CANCELLED_BY_CLIENT("Отменена клиентом"),

    /** Отменена юристом */
    CANCELLED_BY_LAWYER("Отменена юристом"),

    /** Клиент не явился */
    NO_SHOW("Клиент не явился"),

    /** Перенесена */
    RESCHEDULED("Перенесена");

    /** Русское название статуса */
    private final String displayName;

    /**
     * Конструктор enum.
     *
     * @param displayName русское название статуса
     */
    ConsultationStatus(String displayName) {
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
