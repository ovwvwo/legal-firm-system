package ru.fa.legal.service;

// Импорт Spring аннотаций
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Импорт моделей и репозиториев
import ru.fa.legal.model.Consultation;
import ru.fa.legal.model.ConsultationStatus;
import ru.fa.legal.model.User;
import ru.fa.legal.repository.ConsultationRepository;
import ru.fa.legal.repository.UserRepository;

// Импорт классов для работы с коллекциями и датами
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Сервис для работы с консультациями.
 * Содержит бизнес-логику управления консультациями.
 *
 * @author Иванов Егор Борисович
 * @version 1.0
 */
@Service
@Transactional
public class ConsultationService {

    /**
     * Репозиторий для доступа к данным консультаций.
     */
    @Autowired
    private ConsultationRepository consultationRepository;

    /**
     * Репозиторий для доступа к данным пользователей.
     */
    @Autowired
    private UserRepository userRepository;

    /**
     * Получить все консультации.
     *
     * @return список всех консультаций
     */
    @Transactional(readOnly = true)
    public List<Consultation> getAllConsultations() {
        return consultationRepository.findAll();
    }

    /**
     * Получить консультацию по ID.
     *
     * @param id идентификатор консультации
     * @return консультация
     * @throws RuntimeException если консультация не найдена
     */
    @Transactional(readOnly = true)
    public Consultation getConsultationById(Long id) {
        return consultationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Консультация с ID " + id + " не найдена"));
    }

    /**
     * Получить консультации клиента.
     *
     * @param clientId идентификатор клиента
     * @return список консультаций клиента
     */
    @Transactional(readOnly = true)
    public List<Consultation> getConsultationsByClient(Long clientId) {
        // Получаем клиента
        User client = userRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Клиент с ID " + clientId + " не найден"));

        // Возвращаем консультации клиента
        return consultationRepository.findByClient(client);
    }

    /**
     * Получить консультации юриста.
     *
     * @param lawyerId идентификатор юриста
     * @return список консультаций юриста
     */
    @Transactional(readOnly = true)
    public List<Consultation> getConsultationsByLawyer(Long lawyerId) {
        // Получаем юриста
        User lawyer = userRepository.findById(lawyerId)
                .orElseThrow(() -> new RuntimeException("Юрист с ID " + lawyerId + " не найден"));

        // Возвращаем консультации юриста
        return consultationRepository.findByLawyer(lawyer);
    }

    /**
     * Получить консультации по статусу.
     *
     * @param status статус консультации
     * @return список консультаций с указанным статусом
     */
    @Transactional(readOnly = true)
    public List<Consultation> getConsultationsByStatus(ConsultationStatus status) {
        return consultationRepository.findByStatus(status);
    }

    /**
     * Получить предстоящие консультации юриста.
     *
     * @param lawyerId идентификатор юриста
     * @return список предстоящих консультаций
     */
    @Transactional(readOnly = true)
    public List<Consultation> getUpcomingConsultationsByLawyer(Long lawyerId) {
        // Получаем юриста
        User lawyer = userRepository.findById(lawyerId)
                .orElseThrow(() -> new RuntimeException("Юрист с ID " + lawyerId + " не найден"));

        // Возвращаем консультации после текущего времени
        return consultationRepository.findUpcomingConsultationsByLawyer(lawyer, LocalDateTime.now());
    }

    /**
     * Получить консультации в указанном диапазоне дат.
     *
     * @param startDate начальная дата и время
     * @param endDate конечная дата и время
     * @return список консультаций
     */
    @Transactional(readOnly = true)
    public List<Consultation> getConsultationsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return consultationRepository.findByConsultationDateBetween(startDate, endDate);
    }

    /**
     * Создать новую консультацию.
     *
     * @param consultation новая консультация
     * @return сохраненная консультация
     * @throws RuntimeException если данные некорректны
     */
    public Consultation createConsultation(Consultation consultation) {
        // Проверяем наличие клиента
        if (consultation.getClient() == null || consultation.getClient().getId() == null) {
            throw new RuntimeException("Клиент должен быть указан");
        }

        // Проверяем существование клиента
        if (!userRepository.existsById(consultation.getClient().getId())) {
            throw new RuntimeException("Клиент с ID " + consultation.getClient().getId() + " не найден");
        }

        // Если указан юрист, проверяем его существование
        if (consultation.getLawyer() != null && consultation.getLawyer().getId() != null) {
            if (!userRepository.existsById(consultation.getLawyer().getId())) {
                throw new RuntimeException("Юрист с ID " + consultation.getLawyer().getId() + " не найден");
            }

            // Проверяем доступность юриста на указанное время
            if (isLawyerBusy(consultation.getLawyer().getId(),
                    consultation.getConsultationDate(),
                    consultation.getDurationMinutes())) {
                throw new RuntimeException("Юрист занят в указанное время");
            }
        }

        // Проверяем, что дата консультации в будущем
        if (consultation.getConsultationDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Дата консультации должна быть в будущем");
        }

        // Сохраняем консультацию
        return consultationRepository.save(consultation);
    }

    /**
     * Обновить существующую консультацию.
     *
     * @param id идентификатор консультации
     * @param updatedConsultation обновленные данные консультации
     * @return обновленная консультация
     * @throws RuntimeException если консультация не найдена
     */
    public Consultation updateConsultation(Long id, Consultation updatedConsultation) {
        // Получаем существующую консультацию
        Consultation existingConsultation = getConsultationById(id);

        // Обновляем поля консультации
        existingConsultation.setConsultationDate(updatedConsultation.getConsultationDate());
        existingConsultation.setDurationMinutes(updatedConsultation.getDurationMinutes());
        existingConsultation.setTopic(updatedConsultation.getTopic());
        existingConsultation.setDescription(updatedConsultation.getDescription());
        existingConsultation.setType(updatedConsultation.getType());
        existingConsultation.setStatus(updatedConsultation.getStatus());
        existingConsultation.setCost(updatedConsultation.getCost());
        existingConsultation.setLawyerNotes(updatedConsultation.getLawyerNotes());
        existingConsultation.setResult(updatedConsultation.getResult());
        existingConsultation.setIsPaid(updatedConsultation.getIsPaid());

        // Если изменился юрист, проверяем его доступность
        if (updatedConsultation.getLawyer() != null &&
                !updatedConsultation.getLawyer().getId().equals(existingConsultation.getLawyer().getId())) {

            if (isLawyerBusy(updatedConsultation.getLawyer().getId(),
                    updatedConsultation.getConsultationDate(),
                    updatedConsultation.getDurationMinutes(),
                    id)) { // Исключаем текущую консультацию из проверки
                throw new RuntimeException("Юрист занят в указанное время");
            }

            existingConsultation.setLawyer(updatedConsultation.getLawyer());
        }

        // Сохраняем обновленную консультацию
        return consultationRepository.save(existingConsultation);
    }

    /**
     * Удалить консультацию.
     *
     * @param id идентификатор консультации
     * @throws RuntimeException если консультация не найдена
     */
    public void deleteConsultation(Long id) {
        // Проверяем существование консультации
        if (!consultationRepository.existsById(id)) {
            throw new RuntimeException("Консультация с ID " + id + " не найдена");
        }

        // Удаляем консультацию
        consultationRepository.deleteById(id);
    }

    /**
     * Изменить статус консультации.
     *
     * @param id идентификатор консультации
     * @param newStatus новый статус
     * @return обновленная консультация
     */
    public Consultation changeConsultationStatus(Long id, ConsultationStatus newStatus) {
        // Получаем консультацию
        Consultation consultation = getConsultationById(id);

        // Устанавливаем новый статус
        consultation.setStatus(newStatus);

        // Сохраняем изменения
        return consultationRepository.save(consultation);
    }

    /**
     * Назначить юриста на консультацию.
     *
     * @param consultationId идентификатор консультации
     * @param lawyerId идентификатор юриста
     * @return обновленная консультация
     */
    public Consultation assignLawyer(Long consultationId, Long lawyerId) {
        // Получаем консультацию
        Consultation consultation = getConsultationById(consultationId);

        // Получаем юриста
        User lawyer = userRepository.findById(lawyerId)
                .orElseThrow(() -> new RuntimeException("Юрист с ID " + lawyerId + " не найден"));

        // Проверяем доступность юриста
        if (isLawyerBusy(lawyerId,
                consultation.getConsultationDate(),
                consultation.getDurationMinutes(),
                consultationId)) {
            throw new RuntimeException("Юрист занят в указанное время");
        }

        // Назначаем юриста
        consultation.setLawyer(lawyer);

        // Сохраняем изменения
        return consultationRepository.save(consultation);
    }

    /**
     * Отметить консультацию как оплаченную.
     *
     * @param id идентификатор консультации
     * @return обновленная консультация
     */
    public Consultation markAsPaid(Long id) {
        // Получаем консультацию
        Consultation consultation = getConsultationById(id);

        // Устанавливаем флаг оплаты
        consultation.setIsPaid(true);

        // Сохраняем изменения
        return consultationRepository.save(consultation);
    }

    /**
     * Отправить напоминание о консультации.
     *
     * @param id идентификатор консультации
     * @return обновленная консультация
     */
    public Consultation sendReminder(Long id) {
        // Получаем консультацию
        Consultation consultation = getConsultationById(id);

        // Здесь должна быть логика отправки напоминания (email, SMS и т.д.)
        // Для примера просто устанавливаем флаг

        // Устанавливаем флаг отправки напоминания
        consultation.setReminderSent(true);

        // Сохраняем изменения
        return consultationRepository.save(consultation);
    }

    /**
     * Проверить, занят ли юрист в указанное время.
     *
     * @param lawyerId идентификатор юриста
     * @param dateTime дата и время консультации
     * @param durationMinutes продолжительность в минутах
     * @return true, если юрист занят
     */
    private boolean isLawyerBusy(Long lawyerId, LocalDateTime dateTime, Integer durationMinutes) {
        return isLawyerBusy(lawyerId, dateTime, durationMinutes, null);
    }

    /**
     * Проверить, занят ли юрист в указанное время (с исключением консультации).
     *
     * @param lawyerId идентификатор юриста
     * @param dateTime дата и время консультации
     * @param durationMinutes продолжительность в минутах
     * @param excludeConsultationId ID консультации, которую нужно исключить из проверки
     * @return true, если юрист занят
     */
    private boolean isLawyerBusy(Long lawyerId, LocalDateTime dateTime, Integer durationMinutes, Long excludeConsultationId) {
        // Получаем юриста
        User lawyer = userRepository.findById(lawyerId)
                .orElseThrow(() -> new RuntimeException("Юрист с ID " + lawyerId + " не найден"));

        // Вычисляем время окончания новой консультации
        LocalDateTime endTime = dateTime.plusMinutes(durationMinutes);

        // Получаем все консультации юриста в этот день
        LocalDateTime dayStart = dateTime.toLocalDate().atStartOfDay();
        LocalDateTime dayEnd = dayStart.plusDays(1);

        List<Consultation> consultations = consultationRepository
                .findByConsultationDateBetween(dayStart, dayEnd)
                .stream()
                .filter(c -> c.getLawyer() != null && c.getLawyer().getId().equals(lawyerId))
                .filter(c -> excludeConsultationId == null || !c.getId().equals(excludeConsultationId))
                .filter(c -> c.getStatus() == ConsultationStatus.SCHEDULED ||
                        c.getStatus() == ConsultationStatus.CONFIRMED ||
                        c.getStatus() == ConsultationStatus.IN_PROGRESS)
                .collect(Collectors.toList());

        // Проверяем пересечение времени с существующими консультациями
        for (Consultation consultation : consultations) {
            LocalDateTime existingStart = consultation.getConsultationDate();
            LocalDateTime existingEnd = consultation.getEndTime();

            // Проверяем пересечение интервалов времени
            if (!(endTime.isBefore(existingStart) || dateTime.isAfter(existingEnd))) {
                return true; // Время занято
            }
        }

        return false; // Время свободно
    }

    /**
     * Получить количество консультаций.
     *
     * @return общее количество консультаций
     */
    @Transactional(readOnly = true)
    public long getConsultationCount() {
        return consultationRepository.count();
    }

    /**
     * Получить количество консультаций клиента.
     *
     * @param clientId идентификатор клиента
     * @return количество консультаций клиента
     */
    @Transactional(readOnly = true)
    public long getConsultationCountByClient(Long clientId) {
        User client = userRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Клиент с ID " + clientId + " не найден"));
        return consultationRepository.countByClient(client);
    }

    /**
     * Получить количество консультаций юриста.
     *
     * @param lawyerId идентификатор юриста
     * @return количество консультаций юриста
     */
    @Transactional(readOnly = true)
    public long getConsultationCountByLawyer(Long lawyerId) {
        User lawyer = userRepository.findById(lawyerId)
                .orElseThrow(() -> new RuntimeException("Юрист с ID " + lawyerId + " не найден"));
        return consultationRepository.countByLawyer(lawyer);
    }

    /**
     * Получить количество консультаций по статусу.
     *
     * @param status статус консультации
     * @return количество консультаций
     */
    @Transactional(readOnly = true)
    public long getConsultationCountByStatus(ConsultationStatus status) {
        return consultationRepository.countByStatus(status);
    }

    /**
     * Получить неоплаченные консультации.
     *
     * @return список неоплаченных консультаций
     */
    @Transactional(readOnly = true)
    public List<Consultation> getUnpaidConsultations() {
        return consultationRepository.findByIsPaid(false);
    }

    /**
     * Получить предстоящие консультации (ограниченное количество).
     *
     * @param limit максимальное количество консультаций
     * @return список предстоящих консультаций
     */
    @Transactional(readOnly = true)
    public List<Consultation> getUpcomingConsultations(int limit) {
        // Получаем все консультации после текущего времени
        return consultationRepository.findAll().stream()
                .filter(c -> c.getConsultationDate().isAfter(LocalDateTime.now()))
                .filter(c -> c.getStatus() == ConsultationStatus.SCHEDULED ||
                        c.getStatus() == ConsultationStatus.CONFIRMED)
                .sorted((c1, c2) -> c1.getConsultationDate().compareTo(c2.getConsultationDate()))
                .limit(limit)
                .collect(Collectors.toList());
    }
}