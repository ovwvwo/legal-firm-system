package ru.fa.legal.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.fa.legal.model.User;
import ru.fa.legal.model.UserRole;
import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с пользователями.
 * Обеспечивает доступ к данным пользователей в БД.
 * JpaRepository предоставляет стандартные CRUD операции и методы запросов.
 *
 * @author Киселева Ольга
 * @version 1.0
 */
@Repository
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