// ============================================
// CaseRepository.java
// ============================================
package ru.fa.legal.repository;

// Импорт Spring Data JPA
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
// Импорт моделей
import ru.fa.legal.model.Case;
import ru.fa.legal.model.CaseStatus;
import ru.fa.legal.model.CaseCategory;
import ru.fa.legal.model.User;

// Импорт классов для работы с датами и коллекциями
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с делами.
 * Обеспечивает доступ к данным дел в БД.
 *
 * @author Иванов Егор Борисович
 * @version 1.0
 */
@Repository
public interface CaseRepository extends JpaRepository<Case, Long> {

    /**
     * Найти дело по номеру.
     *
     * @param caseNumber номер дела
     * @return Optional с делом или пустой Optional
     */
    Optional<Case> findByCaseNumber(String caseNumber);

    /**
     * Найти все дела клиента.
     *
     * @param client клиент
     * @return список дел клиента
     */
    List<Case> findByClient(User client);

    /**
     * Найти все дела юриста.
     *
     * @param lawyer юрист
     * @return список дел юриста
     */
    List<Case> findByLawyer(User lawyer);

    /**
     * Найти дела по статусу.
     *
     * @param status статус дела
     * @return список дел с указанным статусом
     */
    List<Case> findByStatus(CaseStatus status);

    /**
     * Найти дела по категории.
     *
     * @param category категория дела
     * @return список дел с указанной категорией
     */
    List<Case> findByCategory(CaseCategory category);

    /**
     * Найти дела клиента по статусу.
     *
     * @param client клиент
     * @param status статус дела
     * @return список дел
     */
    List<Case> findByClientAndStatus(User client, CaseStatus status);

    /**
     * Найти дела юриста по статусу.
     *
     * @param lawyer юрист
     * @param status статус дела
     * @return список дел
     */
    List<Case> findByLawyerAndStatus(User lawyer, CaseStatus status);

    /**
     * Найти дела с предстоящими заседаниями.
     * Использует JPQL запрос для поиска дел с заседаниями после указанной даты.
     *
     * @param date дата для сравнения
     * @return список дел с предстоящими заседаниями
     */
    // @Query - позволяет написать собственный JPQL запрос
    @Query("SELECT c FROM Case c WHERE c.nextHearingDate >= :date ORDER BY c.nextHearingDate")
    List<Case> findUpcomingHearings(@Param("date") LocalDate date);

    /**
     * Найти дела, открытые в указанном диапазоне дат.
     *
     * @param startDate начальная дата
     * @param endDate конечная дата
     * @return список дел
     */
    @Query("SELECT c FROM Case c WHERE c.openDate BETWEEN :startDate AND :endDate")
    List<Case> findByOpenDateBetween(@Param("startDate") LocalDate startDate,
                                     @Param("endDate") LocalDate endDate);

    /**
     * Подсчитать количество дел по статусу.
     *
     * @param status статус дела
     * @return количество дел
     */
    long countByStatus(CaseStatus status);

    /**
     * Подсчитать количество дел клиента.
     *
     * @param client клиент
     * @return количество дел
     */
    long countByClient(User client);

    /**
     * Подсчитать количество дел юриста.
     *
     * @param lawyer юрист
     * @return количество дел
     */
    long countByLawyer(User lawyer);

    /**
     * Проверить существование дела с заданным номером.
     *
     * @param caseNumber номер дела
     * @return true, если дело существует
     */
    boolean existsByCaseNumber(String caseNumber);
}