package ru.fa.legal.model;

// Импорт аннотаций JPA
import jakarta.persistence.*;
// Импорт аннотаций валидации
import jakarta.validation.constraints.*;
// Импорт Lombok аннотаций
import lombok.*;

// Импорт классов для работы с датами
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Сущность документа.
 * Представляет документ, связанный с делом (дочерняя сущность).
 * Реализует связь "родитель-дочка" с делом (Case).
 *
 * @author Иванов Егор Борисович
 * @version 1.0
 */
// @Entity - помечает класс как сущность JPA
@Entity
// @Table - указывает имя таблицы и индексы
@Table(name = "documents", indexes = {
        // Индекс по типу документа для быстрой фильтрации
        @Index(name = "idx_document_type", columnList = "documentType")
})
// Lombok аннотации
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Document {

    /**
     * Уникальный идентификатор документа.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Дело, к которому относится документ.
     * Связь "многие к одному" - много документов могут относиться к одному делу.
     * Это реализация дочерней части связи "родитель-дочка".
     */
    // @ManyToOne - определяет связь "многие к одному"
    // fetch = LAZY - ленивая загрузка связанной сущности
    @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn - указывает столбец для связи
    // name = "case_id" - имя столбца внешнего ключа
    // nullable = false - связь обязательна (документ всегда принадлежит делу)
    @JoinColumn(name = "case_id", nullable = false)
    @NotNull(message = "Дело обязательно для указания")
    private Case caseEntity;

    /**
     * Название документа.
     */
    @Column(nullable = false, length = 200)
    @NotBlank(message = "Название документа обязательно для заполнения")
    @Size(max = 200, message = "Название документа не должно превышать 200 символов")
    private String title;

    /**
     * Тип документа.
     * Определяет категорию документа.
     *
     * @see DocumentType
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @NotNull(message = "Тип документа обязателен для выбора")
    private DocumentType documentType;

    /**
     * Описание документа.
     * Краткая информация о содержании документа.
     */
    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Путь к файлу документа в файловой системе или хранилище.
     * Может содержать URL или локальный путь.
     */
    @Column(length = 500)
    @Size(max = 500, message = "Путь к файлу не должен превышать 500 символов")
    private String filePath;

    /**
     * Имя файла.
     * Оригинальное имя загруженного файла.
     */
    @Column(length = 255)
    @Size(max = 255, message = "Имя файла не должно превышать 255 символов")
    private String fileName;

    /**
     * Размер файла в байтах.
     */
    @Column
    private Long fileSize;

    /**
     * MIME тип файла (например, application/pdf, image/jpeg).
     */
    @Column(length = 100)
    @Size(max = 100, message = "MIME тип не должен превышать 100 символов")
    private String mimeType;

    /**
     * Дата создания документа (дата подписания, составления и т.д.).
     */
    @Column
    private LocalDate documentDate;

    /**
     * Номер документа (если применимо).
     * Например, номер договора, искового заявления и т.д.
     */
    @Column(length = 100)
    @Size(max = 100, message = "Номер документа не должен превышать 100 символов")
    private String documentNumber;

    /**
     * Флаг важности документа.
     * Определяет, является ли документ критически важным для дела.
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean isImportant = false;

    /**
     * Статус документа.
     * Определяет состояние документа в процессе работы.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private DocumentStatus status = DocumentStatus.DRAFT;

    /**
     * Заметки к документу.
     * Дополнительные комментарии или пометки.
     */
    @Lob
    @Column(columnDefinition = "TEXT")
    private String notes;

    /**
     * Дата и время создания записи в системе.
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Дата и время последнего обновления записи.
     */
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Метод жизненного цикла JPA.
     * Автоматически устанавливает даты при создании сущности.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    /**
     * Метод жизненного цикла JPA.
     * Автоматически обновляет дату изменения при обновлении сущности.
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Переопределение toString для предотвращения циклических ссылок.
     * Исключаем связанное дело из вывода.
     *
     * @return строковое представление объекта
     */
    @Override
    public String toString() {
        return "Document{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", documentType=" + documentType +
                ", fileName='" + fileName + '\'' +
                ", fileSize=" + fileSize +
                ", documentDate=" + documentDate +
                ", documentNumber='" + documentNumber + '\'' +
                ", isImportant=" + isImportant +
                ", status=" + status +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}

/**
 * Перечисление типов документов.
 * Классифицирует документы по их назначению.
 */
enum DocumentType {
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

/**
 * Перечисление статусов документов.
 * Определяет состояние документа в процессе работы.
 */
enum DocumentStatus {
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