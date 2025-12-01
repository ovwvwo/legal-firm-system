package ru.fa.legal.service;

// Импорт Spring аннотаций
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Импорт моделей и репозиториев
import ru.fa.legal.model.Case;
import ru.fa.legal.model.CaseStatus;
import ru.fa.legal.model.CaseCategory;
import ru.fa.legal.model.User;
import ru.fa.legal.repository.CaseRepository;
import ru.fa.legal.repository.UserRepository;

// Импорт классов для работы с коллекциями и датами
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Сервис для работы с делами.
 * Содержит бизнес-логику управления делами.
 *
 * @author Иванов Егор Борисович
 * @version 1.0
 */
@Service
@Transactional
public class CaseService {

    /**
     * Репозиторий для доступа к данным дел.
     */
    @Autowired
    private CaseRepository caseRepository;

    /**
     * Репозиторий для доступа к данным пользователей.
     */
    @Autowired
    private UserRepository userRepository;

    /**
     * Получить все дела.
     *
     * @return список всех дел
     */
    @Transactional(readOnly = true)
    public List<Case> getAllCases() {
        // Вызываем метод findAll() для получения всех записей
        return caseRepository.findAll();
    }

    /**
     * Получить дело по ID.
     *
     * @param id идентификатор дела
     * @return дело
     * @throws RuntimeException если дело не найдено
     */
    @Transactional(readOnly = true)
    public Case getCaseById(Long id) {
        // Используем orElseThrow для генерации исключения при отсутствии дела
        return caseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Дело с ID " + id + " не найдено"));
    }

    /**
     * Получить дело по номеру.
     *
     * @param caseNumber номер дела
     * @return дело
     * @throws RuntimeException если дело не найдено
     */
    @Transactional(readOnly = true)
    public Case getCaseByCaseNumber(String caseNumber) {
        return caseRepository.findByCaseNumber(caseNumber)
                .orElseThrow(() -> new RuntimeException("Дело с номером " + caseNumber + " не найдено"));
    }

    /**
     * Получить дела клиента.
     *
     * @param clientId идентификатор клиента
     * @return список дел клиента
     */
    @Transactional(readOnly = true)
    public List<Case> getCasesByClient(Long clientId) {
        // Получаем клиента
        User client = userRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Клиент с ID " + clientId + " не найден"));

        // Возвращаем дела клиента
        return caseRepository.findByClient(client);
    }

    /**
     * Получить дела юриста.
     *
     * @param lawyerId идентификатор юриста
     * @return список дел юриста
     */
    @Transactional(readOnly = true)
    public List<Case> getCasesByLawyer(Long lawyerId) {
        // Получаем юриста
        User lawyer = userRepository.findById(lawyerId)
                .orElseThrow(() -> new RuntimeException("Юрист с ID " + lawyerId + " не найден"));

        // Возвращаем дела юриста
        return caseRepository.findByLawyer(lawyer);
    }

    /**
     * Получить дела по статусу.
     *
     * @param status статус дела
     * @return список дел с указанным статусом
     */
    @Transactional(readOnly = true)
    public List<Case> getCasesByStatus(CaseStatus status) {
        return caseRepository.findByStatus(status);
    }

    /**
     * Получить дела по категории.
     *
     * @param category категория дела
     * @return список дел с указанной категорией
     */
    @Transactional(readOnly = true)
    public List<Case> getCasesByCategory(CaseCategory category) {
        return caseRepository.findByCategory(category);
    }

    /**
     * Создать новое дело.
     *
     * @param caseEntity новое дело
     * @return сохраненное дело
     * @throws RuntimeException если дело с таким номером уже существует
     */
    public Case createCase(Case caseEntity) {
        // Проверяем уникальность номера дела
        if (caseRepository.existsByCaseNumber(caseEntity.getCaseNumber())) {
            throw new RuntimeException("Дело с номером " + caseEntity.getCaseNumber() + " уже существует");
        }

        // Проверяем наличие клиента
        if (caseEntity.getClient() == null || caseEntity.getClient().getId() == null) {
            throw new RuntimeException("Клиент должен быть указан");
        }

        // Проверяем существование клиента
        if (!userRepository.existsById(caseEntity.getClient().getId())) {
            throw new RuntimeException("Клиент с ID " + caseEntity.getClient().getId() + " не найден");
        }

        // Если указан юрист, проверяем его существование
        if (caseEntity.getLawyer() != null && caseEntity.getLawyer().getId() != null) {
            if (!userRepository.existsById(caseEntity.getLawyer().getId())) {
                throw new RuntimeException("Юрист с ID " + caseEntity.getLawyer().getId() + " не найден");
            }
        }

        // Устанавливаем дату открытия, если не указана
        if (caseEntity.getOpenDate() == null) {
            caseEntity.setOpenDate(LocalDate.now());
        }

        // Сохраняем дело в базе данных
        return caseRepository.save(caseEntity);
    }

    /**
     * Обновить существующее дело.
     *
     * @param id идентификатор дела
     * @param updatedCase обновленные данные дела
     * @return обновленное дело
     * @throws RuntimeException если дело не найдено
     */
    public Case updateCase(Long id, Case updatedCase) {
        // Получаем существующее дело
        Case existingCase = getCaseById(id);

        // Проверяем уникальность номера, если он изменился
        if (!existingCase.getCaseNumber().equals(updatedCase.getCaseNumber()) &&
                caseRepository.existsByCaseNumber(updatedCase.getCaseNumber())) {
            throw new RuntimeException("Дело с номером " + updatedCase.getCaseNumber() + " уже существует");
        }

        // Обновляем поля существующего дела
        existingCase.setCaseNumber(updatedCase.getCaseNumber());
        existingCase.setTitle(updatedCase.getTitle());
        existingCase.setDescription(updatedCase.getDescription());
        existingCase.setCategory(updatedCase.getCategory());
        existingCase.setStatus(updatedCase.getStatus());
        existingCase.setClient(updatedCase.getClient());
        existingCase.setLawyer(updatedCase.getLawyer());
        existingCase.setOpenDate(updatedCase.getOpenDate());
        existingCase.setCloseDate(updatedCase.getCloseDate());
        existingCase.setCost(updatedCase.getCost());
        existingCase.setPriority(updatedCase.getPriority());
        existingCase.setNextHearingDate(updatedCase.getNextHearingDate());
        existingCase.setNotes(updatedCase.getNotes());

        // Сохраняем обновленное дело
        return caseRepository.save(existingCase);
    }

    /**
     * Удалить дело.
     *
     * @param id идентификатор дела
     * @throws RuntimeException если дело не найдено
     */
    public void deleteCase(Long id) {
        // Проверяем существование дела
        if (!caseRepository.existsById(id)) {
            throw new RuntimeException("Дело с ID " + id + " не найдено");
        }

        // Удаляем дело (каскадно удалятся все связанные документы)
        caseRepository.deleteById(id);
    }

    /**
     * Изменить статус дела.
     *
     * @param id идентификатор дела
     * @param newStatus новый статус
     * @return обновленное дело
     */
    public Case changeCaseStatus(Long id, CaseStatus newStatus) {
        // Получаем дело
        Case caseEntity = getCaseById(id);

        // Устанавливаем новый статус
        caseEntity.setStatus(newStatus);

        // Если дело закрывается, устанавливаем дату закрытия
        if (newStatus == CaseStatus.CLOSED ||
                newStatus == CaseStatus.WON ||
                newStatus == CaseStatus.LOST ||
                newStatus == CaseStatus.SETTLED) {
            if (caseEntity.getCloseDate() == null) {
                caseEntity.setCloseDate(LocalDate.now());
            }
        }

        // Сохраняем изменения
        return caseRepository.save(caseEntity);
    }

    /**
     * Назначить юриста на дело.
     *
     * @param caseId идентификатор дела
     * @param lawyerId идентификатор юриста
     * @return обновленное дело
     */
    public Case assignLawyer(Long caseId, Long lawyerId) {
        // Получаем дело
        Case caseEntity = getCaseById(caseId);

        // Получаем юриста
        User lawyer = userRepository.findById(lawyerId)
                .orElseThrow(() -> new RuntimeException("Юрист с ID " + lawyerId + " не найден"));

        // Назначаем юриста
        caseEntity.setLawyer(lawyer);

        // Сохраняем изменения
        return caseRepository.save(caseEntity);
    }

    /**
     * Поиск дел по ключевому слову.
     * Ищет в названии, описании и номере дела.
     *
     * @param keyword ключевое слово для поиска
     * @return список найденных дел
     */
    @Transactional(readOnly = true)
    public List<Case> searchCasesByKeyword(String keyword) {
        // Получаем все дела
        List<Case> allCases = caseRepository.findAll();

        // Приводим ключевое слово к нижнему регистру для поиска без учета регистра
        String lowerKeyword = keyword.toLowerCase();

        // Фильтруем дела, используя Stream API
        return allCases.stream()
                .filter(c ->
                        // Проверяем наличие ключевого слова в номере дела
                        c.getCaseNumber().toLowerCase().contains(lowerKeyword) ||
                                // Проверяем наличие ключевого слова в названии
                                c.getTitle().toLowerCase().contains(lowerKeyword) ||
                                // Проверяем наличие ключевого слова в описании
                                c.getDescription().toLowerCase().contains(lowerKeyword)
                )
                .collect(Collectors.toList()); // Собираем результат в список
    }

    /**
     * Получить предстоящие судебные заседания.
     *
     * @return список дел с предстоящими заседаниями
     */
    @Transactional(readOnly = true)
    public List<Case> getUpcomingHearings() {
        // Получаем дела с заседаниями после текущей даты
        return caseRepository.findUpcomingHearings(LocalDate.now());
    }

    /**
     * Получить дела, открытые в указанном диапазоне дат.
     *
     * @param startDate начальная дата
     * @param endDate конечная дата
     * @return список дел
     */
    @Transactional(readOnly = true)
    public List<Case> getCasesByDateRange(LocalDate startDate, LocalDate endDate) {
        return caseRepository.findByOpenDateBetween(startDate, endDate);
    }

    /**
     * Получить количество дел.
     *
     * @return общее количество дел
     */
    @Transactional(readOnly = true)
    public long getCaseCount() {
        return caseRepository.count();
    }

    /**
     * Получить количество дел по статусу.
     *
     * @param status статус дела
     * @return количество дел с указанным статусом
     */
    @Transactional(readOnly = true)
    public long getCaseCountByStatus(CaseStatus status) {
        return caseRepository.countByStatus(status);
    }

    /**
     * Получить количество дел клиента.
     *
     * @param clientId идентификатор клиента
     * @return количество дел клиента
     */
    @Transactional(readOnly = true)
    public long getCaseCountByClient(Long clientId) {
        User client = userRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Клиент с ID " + clientId + " не найден"));
        return caseRepository.countByClient(client);
    }

    /**
     * Получить количество дел юриста.
     *
     * @param lawyerId идентификатор юриста
     * @return количество дел юриста
     */
    @Transactional(readOnly = true)
    public long getCaseCountByLawyer(Long lawyerId) {
        User lawyer = userRepository.findById(lawyerId)
                .orElseThrow(() -> new RuntimeException("Юрист с ID " + lawyerId + " не найден"));
        return caseRepository.countByLawyer(lawyer);
    }

    /**
     * Получить последние дела (ограниченное количество).
     *
     * @param limit максимальное количество дел
     * @return список последних дел
     */
    @Transactional(readOnly = true)
    public List<Case> getRecentCases(int limit) {
        // Получаем все дела и сортируем по дате создания
        return caseRepository.findAll().stream()
                .sorted((c1, c2) -> c2.getCreatedAt().compareTo(c1.getCreatedAt())) // Сортировка по убыванию
                .limit(limit) // Ограничиваем количество
                .collect(Collectors.toList());
    }
}