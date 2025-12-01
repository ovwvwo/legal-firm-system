package ru.fa.legal.service;

// Импорт Spring аннотаций
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Импорт моделей и репозиториев
import ru.fa.legal.model.*;
import ru.fa.legal.repository.*;

// Импорт классов для работы с коллекциями и датами
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Сервис для работы со статистикой.
 * Предоставляет различные статистические данные о системе.
 * Реализует требование по статистическим функциям из задания.
 *
 * @author Иванов Егор Борисович
 * @version 1.0
 */
@Service
@Transactional(readOnly = true)
public class StatisticsService {

    /**
     * Репозиторий пользователей.
     */
    @Autowired
    private UserRepository userRepository;

    /**
     * Репозиторий дел.
     */
    @Autowired
    private CaseRepository caseRepository;

    /**
     * Репозиторий документов.
     */
    @Autowired
    private DocumentRepository documentRepository;

    /**
     * Репозиторий консультаций.
     */
    @Autowired
    private ConsultationRepository consultationRepository;

    /**
     * Получить общее количество пользователей системы.
     * Реализует требование: "общее количество пользователей системы".
     *
     * @return количество пользователей
     */
    public long getTotalUserCount() {
        // Подсчитываем всех пользователей
        return userRepository.count();
    }

    /**
     * Получить количество пользователей по ролям.
     *
     * @return карта "роль -> количество"
     */
    public Map<UserRole, Long> getUserCountByRole() {
        // Создаем карту для хранения результатов
        Map<UserRole, Long> result = new HashMap<>();

        // Для каждой роли подсчитываем количество пользователей
        for (UserRole role : UserRole.values()) {
            long count = userRepository.findByRole(role).size();
            result.put(role, count);
        }

        return result;
    }

    /**
     * Получить общее количество дел.
     *
     * @return количество дел
     */
    public long getTotalCaseCount() {
        return caseRepository.count();
    }

    /**
     * Получить количество дел по статусам.
     *
     * @return карта "статус -> количество"
     */
    public Map<CaseStatus, Long> getCaseCountByStatus() {
        // Создаем карту для результатов
        Map<CaseStatus, Long> result = new HashMap<>();

        // Для каждого статуса подсчитываем количество дел
        for (CaseStatus status : CaseStatus.values()) {
            long count = caseRepository.countByStatus(status);
            result.put(status, count);
        }

        return result;
    }

    /**
     * Получить количество дел по категориям.
     *
     * @return карта "категория -> количество"
     */
    public Map<CaseCategory, Long> getCaseCountByCategory() {
        // Создаем карту для результатов
        Map<CaseCategory, Long> result = new HashMap<>();

        // Для каждой категории подсчитываем количество дел
        for (CaseCategory category : CaseCategory.values()) {
            long count = caseRepository.findByCategory(category).size();
            result.put(category, count);
        }

        return result;
    }

    /**
     * Получить среднее количество дел на юриста.
     * Реализует требование: "среднее время ожидания чего-либо".
     *
     * @return среднее количество дел на юриста
     */
    public double getAverageCasesPerLawyer() {
        // Получаем всех активных юристов
        List<User> lawyers = userRepository.findByRoleAndEnabled(UserRole.LAWYER, true);

        // Если юристов нет, возвращаем 0
        if (lawyers.isEmpty()) {
            return 0.0;
        }

        // Получаем общее количество дел
        long totalCases = caseRepository.count();

        // Вычисляем среднее значение
        double average = (double) totalCases / lawyers.size();

        // Округляем до 2 знаков после запятой
        return Math.round(average * 100.0) / 100.0;
    }

    /**
     * Получить общее количество консультаций.
     *
     * @return количество консультаций
     */
    public long getTotalConsultationCount() {
        return consultationRepository.count();
    }

    /**
     * Получить количество консультаций по статусам.
     *
     * @return карта "статус -> количество"
     */
    public Map<ConsultationStatus, Long> getConsultationCountByStatus() {
        // Создаем карту для результатов
        Map<ConsultationStatus, Long> result = new HashMap<>();

        // Для каждого статуса подсчитываем количество консультаций
        for (ConsultationStatus status : ConsultationStatus.values()) {
            long count = consultationRepository.countByStatus(status);
            result.put(status, count);
        }

        return result;
    }

    /**
     * Получить среднюю продолжительность консультации в минутах.
     *
     * @return средняя продолжительность
     */
    public double getAverageConsultationDuration() {
        // Получаем все консультации
        List<Consultation> consultations = consultationRepository.findAll();

        // Если консультаций нет, возвращаем 0
        if (consultations.isEmpty()) {
            return 0.0;
        }

        // Вычисляем среднее значение
        double average = consultations.stream()
                .mapToInt(Consultation::getDurationMinutes)
                .average()
                .orElse(0.0);

        // Округляем до 2 знаков после запятой
        return Math.round(average * 100.0) / 100.0;
    }

    /**
     * Получить общее количество документов.
     *
     * @return количество документов
     */
    public long getTotalDocumentCount() {
        return documentRepository.count();
    }

    /**
     * Получить количество документов по типам.
     *
     * @return карта "тип -> количество"
     */
    public Map<DocumentType, Long> getDocumentCountByType() {
        // Получаем все документы
        List<Document> documents = documentRepository.findAll();

        // Группируем по типам и подсчитываем
        return documents.stream()
                .collect(Collectors.groupingBy(
                        Document::getDocumentType,
                        Collectors.counting()
                ));
    }

    /**
     * Получить топ юристов по количеству дел.
     *
     * @param limit максимальное количество юристов в списке
     * @return список юристов с количеством дел
     */
    public List<Map.Entry<User, Long>> getTopLawyersByCaseCount(int limit) {
        // Получаем всех юристов
        List<User> lawyers = userRepository.findByRole(UserRole.LAWYER);

        // Создаем карту "юрист -> количество дел"
        Map<User, Long> lawyerCaseCount = new HashMap<>();

        for (User lawyer : lawyers) {
            long count = caseRepository.countByLawyer(lawyer);
            lawyerCaseCount.put(lawyer, count);
        }

        // Сортируем по количеству дел и ограничиваем количество
        return lawyerCaseCount.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Получить статистику по делам за период.
     *
     * @param startDate начальная дата
     * @param endDate конечная дата
     * @return карта со статистикой
     */
    public Map<String, Object> getCaseStatisticsForPeriod(LocalDate startDate, LocalDate endDate) {
        // Получаем дела за период
        List<Case> cases = caseRepository.findByOpenDateBetween(startDate, endDate);

        // Создаем карту для результатов
        Map<String, Object> stats = new HashMap<>();

        // Общее количество дел
        stats.put("totalCases", cases.size());

        // Количество по статусам
        Map<CaseStatus, Long> byStatus = cases.stream()
                .collect(Collectors.groupingBy(Case::getStatus, Collectors.counting()));
        stats.put("byStatus", byStatus);

        // Количество по категориям
        Map<CaseCategory, Long> byCategory = cases.stream()
                .collect(Collectors.groupingBy(Case::getCategory, Collectors.counting()));
        stats.put("byCategory", byCategory);

        // Средняя стоимость
        double avgCost = cases.stream()
                .filter(c -> c.getCost() != null)
                .mapToDouble(c -> c.getCost().doubleValue())
                .average()
                .orElse(0.0);
        stats.put("averageCost", Math.round(avgCost * 100.0) / 100.0);

        return stats;
    }

    /**
     * Получить статистику по консультациям за период.
     *
     * @param startDate начальная дата и время
     * @param endDate конечная дата и время
     * @return карта со статистикой
     */
    public Map<String, Object> getConsultationStatisticsForPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        // Получаем консультации за период
        List<Consultation> consultations = consultationRepository.findByConsultationDateBetween(startDate, endDate);

        // Создаем карту для результатов
        Map<String, Object> stats = new HashMap<>();

        // Общее количество консультаций
        stats.put("totalConsultations", consultations.size());

        // Количество по статусам
        Map<ConsultationStatus, Long> byStatus = consultations.stream()
                .collect(Collectors.groupingBy(Consultation::getStatus, Collectors.counting()));
        stats.put("byStatus", byStatus);

        // Средняя стоимость
        double avgCost = consultations.stream()
                .filter(c -> c.getCost() != null)
                .mapToDouble(c -> c.getCost().doubleValue())
                .average()
                .orElse(0.0);
        stats.put("averageCost", Math.round(avgCost * 100.0) / 100.0);

        // Количество оплаченных
        long paidCount = consultations.stream()
                .filter(Consultation::getIsPaid)
                .count();
        stats.put("paidConsultations", paidCount);

        return stats;
    }

    /**
     * Получить общую статистику системы.
     * Возвращает сводку основных показателей.
     *
     * @return карта с общей статистикой
     */
    public Map<String, Object> getGeneralStatistics() {
        // Создаем карту для результатов
        Map<String, Object> stats = new HashMap<>();

        // Статистика по пользователям
        stats.put("totalUsers", getTotalUserCount());
        stats.put("usersByRole", getUserCountByRole());

        // Статистика по делам
        stats.put("totalCases", getTotalCaseCount());
        stats.put("casesByStatus", getCaseCountByStatus());
        stats.put("casesByCategory", getCaseCountByCategory());
        stats.put("averageCasesPerLawyer", getAverageCasesPerLawyer());

        // Статистика по консультациям
        stats.put("totalConsultations", getTotalConsultationCount());
        stats.put("consultationsByStatus", getConsultationCountByStatus());
        stats.put("averageConsultationDuration", getAverageConsultationDuration());

        // Статистика по документам
        stats.put("totalDocuments", getTotalDocumentCount());
        stats.put("documentsByType", getDocumentCountByType());

        return stats;
    }

    /**
     * Получить данные для построения диаграммы распределения дел по статусам.
     * Реализует требование: "допускается реализация гистограммы/диаграммы".
     *
     * @return карта для построения диаграммы
     */
    public Map<String, Object> getCaseStatusChartData() {
        // Получаем количество дел по статусам
        Map<CaseStatus, Long> countByStatus = getCaseCountByStatus();

        // Создаем карту для данных диаграммы
        Map<String, Object> chartData = new HashMap<>();

        // Массив меток (названий статусов)
        List<String> labels = new ArrayList<>();
        // Массив значений (количество дел)
        List<Long> data = new ArrayList<>();

        // Заполняем данные
        for (Map.Entry<CaseStatus, Long> entry : countByStatus.entrySet()) {
            labels.add(entry.getKey().getDisplayName());
            data.add(entry.getValue());
        }

        chartData.put("labels", labels);
        chartData.put("data", data);

        return chartData;
    }

    /**
     * Получить данные для построения диаграммы распределения дел по категориям.
     *
     * @return карта для построения диаграммы
     */
    public Map<String, Object> getCaseCategoryChartData() {
        // Получаем количество дел по категориям
        Map<CaseCategory, Long> countByCategory = getCaseCountByCategory();

        // Создаем карту для данных диаграммы
        Map<String, Object> chartData = new HashMap<>();

        // Массив меток
        List<String> labels = new ArrayList<>();
        // Массив значений
        List<Long> data = new ArrayList<>();

        // Заполняем данные
        for (Map.Entry<CaseCategory, Long> entry : countByCategory.entrySet()) {
            labels.add(entry.getKey().getDisplayName());
            data.add(entry.getValue());
        }

        chartData.put("labels", labels);
        chartData.put("data", data);

        return chartData;
    }
}