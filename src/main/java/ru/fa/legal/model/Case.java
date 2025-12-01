package ru.fa.legal.model;

// Импорт аннотаций JPA для работы с базой данных
import jakarta.persistence.*;
// Импорт аннотаций для валидации данных
import jakarta.validation.constraints.*;
// Импорт аннотаций Lombok для генерации кода
import lombok.*;

// Импорт классов для работы с датами
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
// Импорт классов для работы с коллекциями
import java.util.HashSet;
import java.util.Set;

/**
 * Сущность судебного дела.
 * Представляет информацию о деле клиента в системе.
 * Реализует связь "родитель-дочка" с документами.
 *
 * @author Иванов Егор Борисович
 * @version 1.0
 */
// @Entity - помечает класс как сущность JPA
@Entity
// @Table - указывает имя таблицы и индексы для оптимизации запросов
@Table(name = "cases", indexes = {
        // Создаем индекс по номеру дела для быстрого поиска
        @Index(name = "idx_case_number", columnList = "caseNumber"),
        // Создаем индекс по статусу для фильтрации
        @Index(name = "idx_case_status", columnList = "status")
})
// Lombok аннотации для генерации кода
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Case {

    /**
     * Уникальный идентификатор дела.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Номер дела в системе.
     * Уникальный номер для идентификации дела.
     */
    @Column(nullable = false, unique = true, length = 50)
    @NotBlank(message = "Номер дела обязателен для заполнения")
    @Size(max = 50, message = "Номер дела не должен превышать 50 символов")
    private String caseNumber;

    /**
     * Название дела.
     * Краткое описание сути дела.
     */
    @Column(nullable = false, length = 200)
    @NotBlank(message = "Название дела обязательно для заполнения")
    @Size(max = 200, message = "Название дела не должно превышать 200 символов")
    private String title;

    /**
     * Описание дела.
     * Подробная информация о деле, обстоятельствах, требованиях.
     */
    // @Lob - указывает, что поле должно храниться как большой объект (TEXT в MySQL)
    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    @NotBlank(message = "Описание дела обязательно для заполнения")
    private String description;

    /**
     * Категория дела.
     * Определяет тип юридического дела.
     *
     * @see CaseCategory
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @NotNull(message = "Категория дела обязательна для выбора")
    private CaseCategory category;

    /**
     * Статус дела.
     * Текущее состояние работы над делом.
     *
     * @see CaseStatus
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @NotNull(message = "Статус дела обязателен для выбора")
    @Builder.Default
    private CaseStatus status = CaseStatus.NEW;

    /**
     * Клиент, которому принадлежит дело.
     * Связь "многие к одному" - много дел могут принадлежать одному клиенту.
     */
    // @ManyToOne - определяет связь "многие к одному"
    // @JoinColumn - указывает столбец для связи
    // name = "client_id" - имя столбца внешнего ключа
    // nullable = false - связь обязательна
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    @NotNull(message = "Клиент обязателен для указания")
    private User client;

    /**
     * Юрист, ведущий дело.
     * Связь "многие к одному" - много дел может вести один юрист.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lawyer_id")
    private User lawyer;

    /**
     * Дата открытия дела.
     */
    @Column(nullable = false)
    @NotNull(message = "Дата открытия обязательна для указания")
    private LocalDate openDate;

    /**
     * Дата закрытия дела.
     * Может быть null, если дело еще не закрыто.
     */
    @Column
    private LocalDate closeDate;

    /**
     * Стоимость услуг по делу.
     * Использует BigDecimal для точных финансовых расчетов.
     */
    // @Column с параметрами precision и scale для денежных значений
    // precision = 10 - общее количество цифр
    // scale = 2 - количество цифр после запятой
    @Column(precision = 10, scale = 2)
    @DecimalMin(value = "0.0", message = "Стоимость не может быть отрицательной")
    private BigDecimal cost;

    /**
     * Приоритет дела.
     * Определяет срочность работы над делом.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Priority priority = Priority.MEDIUM;

    /**
     * Дата следующего судебного заседания.
     * Может быть null, если заседание еще не назначено.
     */
    @Column
    private LocalDate nextHearingDate;

    /**
     * Заметки по делу.
     * Дополнительная информация, комментарии.
     */
    @Lob
    @Column(columnDefinition = "TEXT")
    private String notes;

    /**
     * Дата и время создания записи.
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Дата и время последнего обновления записи.
     */
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Коллекция документов, связанных с делом.
     * Реализует связь "один ко многим" (родитель-дочка).
     * Одно дело может иметь много документов.
     *
     * @see Document
     */
    @OneToMany(mappedBy = "caseEntity", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private Set<Document> documents = new HashSet<>();

    /**
     * Метод жизненного цикла JPA.
     * Автоматически устанавливает даты при создании сущности.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        // Если дата открытия не указана, устанавливаем текущую дату
        if (openDate == null) {
            openDate = LocalDate.now();
        }
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
     * Исключаем коллекции и связанные сущности из вывода.
     *
     * @return строковое представление объекта
     */
    @Override
    public String toString() {
        return "Case{" +
                "id=" + id +
                ", caseNumber='" + caseNumber + '\'' +
                ", title='" + title + '\'' +
                ", category=" + category +
                ", status=" + status +
                ", openDate=" + openDate +
                ", closeDate=" + closeDate +
                ", cost=" + cost +
                ", priority=" + priority +
                ", nextHearingDate=" + nextHearingDate +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}

/**
 * Перечисление категорий дел.
 * Классифицирует дела по типу юридической практики.
 */
enum CaseCategory {
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

/**
 * Перечисление статусов дел.
 * Определяет текущее состояние работы над делом.
 */
enum CaseStatus {
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

/**
 * Перечисление приоритетов дел.
 * Определяет срочность работы над делом.
 */
enum Priority {
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