package ru.fa.legal.model;

/**
 * Перечисление категорий дел.
 * Классифицирует дела по типу юридической практики.
 */
public enum CaseCategory {
    /** Гражданские дела */
    CIVIL("Гражданское дело"),

    /** Уголовные дела */
    CRIMINAL("Уголовное дело"),

    /** Административные дела */
    ADMINISTRATIVE("Административное дело"),

    /** Корпоративные дела */
    CORPORATE("Корпоративное дело"),

    /** Семейные дела */
    FAMILY("Семейное дело"),

    /** Трудовые споры */
    LABOR("Трудовой спор"),

    /** Налоговые дела */
    TAX("Налоговое дело"),

    /** Дела по интеллектуальной собственности */
    INTELLECTUAL_PROPERTY("Интеллектуальная собственность");

    /** Русское название категории */
    private final String displayName;

    /**
     * Конструктор enum.
     *
     * @param displayName русское название категории
     */
    CaseCategory(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Получить русское название категории.
     *
     * @return русское название
     */
    public String getDisplayName() {
        return displayName;
    }
}
