package ru.fa.legal.model;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Сущность консультации.
 * Представляет запись на юридическую консультацию.
 *
 * @author Киселева Ольга Ивановна
 * @version 1.0
 */
// @Entity - помечает класс как сущность JPA
@Entity
// @Table - указывает имя таблицы и индексы
@Table(name = "consultations", indexes = {
           @Index(name = "idx_consultation_date", columnList = "consultationDate"),
        @Index(name = "idx_consultation_status", columnList = "status")
})
// Lombok аннотации
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Consultation {

    /**
     * Уникальный идентификатор консультации.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Клиент, записанный на консультацию.
     * Связь "многие к одному" - один клиент может иметь много консультаций.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    @NotNull(message = "Клиент обязателен для указания")
    private User client;

    /**
     * Юрист, проводящий консультацию.
     * Связь "многие к одному" - один юрист может проводить много консультаций.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lawyer_id")
    private User lawyer;

    /**
     * Дата и время консультации.
     */
    @Column(nullable = false)
    @NotNull(message = "Дата и время консультации обязательны для указания")
    private LocalDateTime consultationDate;

    /**
     * Продолжительность консультации в минутах.
     */
    @Column(nullable = false)
    @NotNull(message = "Продолжительность обязательна для указания")
    @Min(value = 15, message = "Продолжительность должна быть не менее 15 минут")
    @Max(value = 480, message = "Продолжительность не должна превышать 480 минут")
    @Builder.Default
    private Integer durationMinutes = 60;

    /**
     * Тема консультации.
     * Краткое описание вопроса клиента.
     */
    @Column(nullable = false, length = 200)
    @NotBlank(message = "Тема консультации обязательна для заполнения")
    @Size(max = 200, message = "Тема не должна превышать 200 символов")
    private String topic;

    /**
     * Подробное описание вопроса.
     */
    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Тип консультации.
     * Определяет формат проведения консультации.
     *
     * @see ConsultationType
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @NotNull(message = "Тип консультации обязателен для выбора")
    @Builder.Default
    private ConsultationType type = ConsultationType.OFFICE;

    /**
     * Статус консультации.
     * Определяет текущее состояние записи.
     *
     * @see ConsultationStatus
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @NotNull(message = "Статус консультации обязателен для выбора")
    @Builder.Default
    private ConsultationStatus status = ConsultationStatus.SCHEDULED;

    /**
     * Стоимость консультации.
     * Использует BigDecimal для точных финансовых расчетов.
     */
    @Column(precision = 10, scale = 2)
    @DecimalMin(value = "0.0", message = "Стоимость не может быть отрицательной")
    private BigDecimal cost;

    /**
     * Заметки юриста после консультации.
     * Краткое резюме обсужденных вопросов и рекомендаций.
     */
    @Lob
    @Column(columnDefinition = "TEXT")
    private String lawyerNotes;

    /**
     * Результат консультации.
     * Итоги и принятые решения.
     */
    @Lob
    @Column(columnDefinition = "TEXT")
    private String result;

    /**
     * Флаг оплаты консультации.
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean isPaid = false;

    /**
     * Напоминание отправлено.
     * Флаг для отслеживания отправки напоминания клиенту.
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean reminderSent = false;

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
     * Вычисляет время окончания консультации.
     *
     * @return дата и время окончания консультации
     */
    public LocalDateTime getEndTime() {
        if (consultationDate == null) {
            return null;
        }
        return consultationDate.plusMinutes(durationMinutes);
    }

    /**
     * Проверяет, является ли консультация предстоящей.
     *
     * @return true, если консультация еще не прошла
     */
    public boolean isUpcoming() {
        if (consultationDate == null) {
            return false;
        }
        return consultationDate.isAfter(LocalDateTime.now());
    }

    /**
     * Проверяет, прошла ли консультация.
     *
     * @return true, если консультация уже завершилась
     */
    public boolean isPast() {
        if (consultationDate == null) {
            return false;
        }
        LocalDateTime endTime = getEndTime();
        if (endTime == null) {
            return false;
        }
        return endTime.isBefore(LocalDateTime.now());
    }

    /**
     * Переопределение toString для предотвращения циклических ссылок.
     *
     * @return строковое представление объекта
     */
    @Override
    public String toString() {
        return "Consultation{" +
                "id=" + id +
                ", consultationDate=" + consultationDate +
                ", durationMinutes=" + durationMinutes +
                ", topic='" + topic + '\'' +
                ", type=" + type +
                ", status=" + status +
                ", cost=" + cost +
                ", isPaid=" + isPaid +
                ", reminderSent=" + reminderSent +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}

/**
 * Перечисление типов консультаций.
 * Определяет формат проведения консультации.
 */
enum ConsultationType {
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

/**
 * Перечисление статусов консультаций.
 * Определяет текущее состояние записи на консультацию.
 */
enum ConsultationStatus {
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