
// ============================================
// UserRepository.java
// ============================================
package ru.fa.legal.repository;

// Импорт Spring Data JPA репозитория
import org.springframework.data.jpa.repository.JpaRepository;
// Импорт аннотации для Spring компонента
import org.springframework.stereotype.Repository;
// Импорт модели пользователя
import ru.fa.legal.model.User;
import ru.fa.legal.model.UserRole;

// Импорт классов для работы с коллекциями
import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с пользователями.
 * Обеспечивает доступ к данным пользователей в БД.
 * JpaRepository предоставляет стандартные CRUD операции и методы запросов.
 *
 * @author Иванов Егор Борисович
 * @version 1.0
 */
// @Repository - помечает интерфейс как компонент доступа к данным Spring
@Repository
// Расширяем JpaRepository с типами User (сущность) и Long (тип ID)
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Найти пользователя по имени пользователя.
     * Используется для авторизации.
     * Spring Data JPA автоматически реализует этот метод на основе имени.
     *
     * @param username имя пользователя
     * @return Optional с пользователем или пустой Optional
     */
    Optional<User> findByUsername(String username);

    /**
     * Найти пользователя по email.
     *
     * @param email email пользователя
     * @return Optional с пользователем или пустой Optional
     */
    Optional<User> findByEmail(String email);

    /**
     * Найти всех пользователей по роли.
     * Используется для получения списка юристов, клиентов и т.д.
     *
     * @param role роль пользователя
     * @return список пользователей с указанной ролью
     */
    List<User> findByRole(UserRole role);

    /**
     * Проверить существование пользователя с заданным именем.
     *
     * @param username имя пользователя
     * @return true, если пользователь существует
     */
    boolean existsByUsername(String username);

    /**
     * Проверить существование пользователя с заданным email.
     *
     * @param email email пользователя
     * @return true, если пользователь существует
     */
    boolean existsByEmail(String email);

    /**
     * Найти активных пользователей.
     *
     * @param enabled флаг активности
     * @return список активных пользователей
     */
    List<User> findByEnabled(Boolean enabled);

    /**
     * Найти пользователей по роли и статусу активности.
     *
     * @param role роль пользователя
     * @param enabled флаг активности
     * @return список пользователей
     */
    List<User> findByRoleAndEnabled(UserRole role, Boolean enabled);
}