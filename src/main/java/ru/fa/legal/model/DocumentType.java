package ru.fa.legal.model;

/**
 * Перечисление типов документов.
 * Классифицирует документы по их назначению.
 */
public enum DocumentType {
    /** Договор */
    CONTRACT("Договор"),

    /** Исковое заявление */
    COMPLAINT("Исковое заявление"),

    /** Судебное решение */
    COURT_DECISION("Судебное решение"),

    /** Протокол судебного заседания */
    COURT_PROTOCOL("Протокол заседания"),

    /** Доверенность */
    POWER_OF_ATTORNEY("Доверенность"),

    /** Заявление */
    APPLICATION("Заявление"),

    /** Ходатайство */
    MOTION("Ходатайство"),

    /** Апелляционная жалоба */
    APPEAL("Апелляционная жалоба"),

    /** Справка */
    CERTIFICATE("Справка"),

    /** Акт */
    ACT("Акт"),

    /** Письмо */
    LETTER("Письмо"),

    /** Иной документ */
    OTHER("Иное");

    /** Русское название типа документа */
    private final String displayName;

    /**
     * Конструктор enum.
     *
     * @param displayName русское название типа документа
     */
    DocumentType(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Получить русское название типа документа.
     *
     * @return русское название
     */
    public String getDisplayName() {
        return displayName;
    }
}
