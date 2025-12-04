package ru.fa.legal.model;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Сущность пользователя системы.
 * Представляет пользователя в базе данных и содержит информацию о его учетных данных.
 *
 * @author Kiseleva Olga
 * @version 1.0
 */
// @Entity - помечает класс как сущность JPA, которая будет сохраняться в БД
@Entity
// @Table - указывает имя таблицы в БД
@Table(name = "users")
// @Data - Lombok аннотация, генерирует геттеры, сеттеры, toString, equals, hashCode
@Data
// @NoArgsConstructor - генерирует конструктор без параметров
@NoArgsConstructor
// @AllArgsConstructor - генерирует конструктор со всеми параметрами
@AllArgsConstructor
// @Builder - генерирует паттерн Builder для удобного создания объектов
@Builder
public class User {

    /**
     * Уникальный идентификатор пользователя.
     * Генерируется автоматически при сохранении в БД.
     */
    // @Id - помечает поле как первичный ключ
    @Id
    // @GeneratedValue - указывает стратегию генерации значения
    // IDENTITY - значение генерируется БД автоматически (AUTO_INCREMENT)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Имя пользователя для входа в систему.
     * Должно быть уникальным.
     */
    // @Column - настройка столбца в БД
    // nullable = false - поле обязательно для заполнения
    // unique = true - значение должно быть уникальным
    // length = 50 - максимальная длина строки
    @Column(nullable = false, unique = true, length = 50)
    // @NotBlank - валидация: поле не должно быть пустым
    @NotBlank(message = "Имя пользователя обязательно для заполнения")
    // @Size - валидация длины строки
    @Size(min = 3, max = 50, message = "Имя пользователя должно содержать от 3 до 50 символов")
    private String username;

    /**
     * Зашифрованный пароль пользователя.
     */
    @Column(nullable = false)
    @NotBlank(message = "Пароль обязателен для заполнения")
    private String password;

    /**
     * Email адрес пользователя.
     * Должен быть уникальным и соответствовать формату email.
     */
    @Column(nullable = false, unique = true, length = 100)
    @NotBlank(message = "Email обязателен для заполнения")
    // @Email - валидация формата email адреса
    @Email(message = "Введите корректный email адрес")
    private String email;

    /**
     * Полное имя пользователя (ФИО).
     */
    @Column(nullable = false, length = 100)
    @NotBlank(message = "ФИО обязательно для заполнения")
    @Size(max = 100, message = "ФИО не должно превышать 100 символов")
    private String fullName;

    /**
     * Номер телефона пользователя.
     */
    @Column(length = 20)
    @Size(max = 20, message = "Номер телефона не должен превышать 20 символов")
    private String phoneNumber;

    /**
     * Роль пользователя в системе.
     * Определяет уровень доступа и функциональные возможности.
     *
     * @see UserRole
     */
    // @Enumerated - указывает, как хранить enum в БД
    // EnumType.STRING - хранить как строку (название enum константы)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    /**
     * Флаг активности учетной записи.
     * Неактивные пользователи не могут входить в систему.
     */
    @Column(nullable = false)
    // @Builder.Default - указывает значение по умолчанию при использовании Builder
    @Builder.Default
    private Boolean enabled = true;

    /**
     * Дата и время создания учетной записи.
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Дата и время последнего обновления учетной записи.
     */
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Коллекция дел, связанных с пользователем.
     * Связь "один ко многим" - один пользователь может иметь много дел.
     *
     * @see Case
     */
    // @OneToMany - определяет связь "один ко многим"
    // mappedBy - указывает поле в связанной сущности, которое владеет связью
    // cascade - операции, которые каскадно применяются к связанным сущностям
    // CascadeType.ALL - все операции (сохранение, удаление и т.д.)
    // fetch - стратегия загрузки данных
    // FetchType.LAZY - ленивая загрузка (данные загружаются только при обращении)
    // orphanRemoval - удалять "осиротевшие" записи (дела без пользователя)
    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    // @Builder.Default - инициализируем пустым множеством
    @Builder.Default
    private Set<Case> cases = new HashSet<>();

    /**
     * Коллекция консультаций, связанных с пользователем.
     * Связь "один ко многим" - один пользователь может иметь много консультаций.
     *
     * @see Consultation
     */
    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private Set<Consultation> consultations = new HashSet<>();

    /**
     * Автоматически устанавливает дату создания перед сохранением сущности в БД.
     * Метод вызывается JPA перед первым сохранением.
     */
    // @PrePersist - метод вызывается перед сохранением новой сущности
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    /**
     * Автоматически обновляет дату изменения перед обновлением сущности в БД.
     * Метод вызывается JPA перед каждым обновлением.
     */
    // @PreUpdate - метод вызывается перед обновлением существующей сущности
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Переопределение toString для предотвращения циклических ссылок.
     * Исключаем коллекции cases и consultations из вывода.
     *
     * @return строковое представление объекта
     */
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", fullName='" + fullName + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", role=" + role +
                ", enabled=" + enabled +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}

