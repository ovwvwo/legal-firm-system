package ru.fa.legal.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.fa.legal.model.Consultation;
import ru.fa.legal.model.ConsultationStatus;
import ru.fa.legal.model.User;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Репозиторий для работы с консультациями.
 * Обеспечивает доступ к данным консультаций в БД.
 *
 * @author Киселева Ольга
 * @version 1.0
 */
@Repository
public interface ConsultationRepository extends JpaRepository<Consultation, Long> {

    /**
     * Найти все консультации клиента.
     *
     * @param client клиент
     * @return список консультаций клиента
     */
    List<Consultation> findByClient(User client);

    /**
     * Найти все консультации юриста.
     *
     * @param lawyer юрист
     * @return список консультаций юриста
     */
    List<Consultation> findByLawyer(User lawyer);

    /**
     * Найти консультации по статусу.
     *
     * @param status статус консультации
     * @return список консультаций
     */
    List<Consultation> findByStatus(ConsultationStatus status);

    /**
     * Найти консультации клиента по статусу.
     *
     * @param client клиент
     * @param status статус консультации
     * @return список консультаций
     */
    List<Consultation> findByClientAndStatus(User client, ConsultationStatus status);

    /**
     * Найти консультации юриста по статусу.
     *
     * @param lawyer юрист
     * @param status статус консультации
     * @return список консультаций
     */
    List<Consultation> findByLawyerAndStatus(User lawyer, ConsultationStatus status);

    /**
     * Найти консультации в указанном диапазоне дат.
     *
     * @param startDate начальная дата
     * @param endDate конечная дата
     * @return список консультаций
     */
    @Query("SELECT c FROM Consultation c WHERE c.consultationDate BETWEEN :startDate AND :endDate ORDER BY c.consultationDate")
    List<Consultation> findByConsultationDateBetween(@Param("startDate") LocalDateTime startDate,
                                                     @Param("endDate") LocalDateTime endDate);

    /**
     * Найти предстоящие консультации юриста.
     *
     * @param lawyer юрист
     * @param currentDate текущая дата и время
     * @return список предстоящих консультаций
     */
    @Query("SELECT c FROM Consultation c WHERE c.lawyer = :lawyer AND c.consultationDate > :currentDate ORDER BY c.consultationDate")
    List<Consultation> findUpcomingConsultationsByLawyer(@Param("lawyer") User lawyer,
                                                         @Param("currentDate") LocalDateTime currentDate);

    /**
     * Найти неоплаченные консультации.
     *
     * @param isPaid флаг оплаты
     * @return список неоплаченных консультаций
     */
    List<Consultation> findByIsPaid(Boolean isPaid);

    /**
     * Подсчитать количество консультаций клиента.
     *
     * @param client клиент
     * @return количество консультаций
     */
    long countByClient(User client);

    /**
     * Подсчитать количество консультаций юриста.
     *
     * @param lawyer юрист
     * @return количество консультаций
     */
    long countByLawyer(User lawyer);

    /**
     * Подсчитать количество консультаций по статусу.
     *
     * @param status статус консультации
     * @return количество консультаций
     */
    long countByStatus(ConsultationStatus status);
}