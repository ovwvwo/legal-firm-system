package ru.fa.legal.controller;

// Импорт Spring MVC аннотаций
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

// Импорт моделей и сервисов
import ru.fa.legal.service.StatisticsService;
import ru.fa.legal.service.ConsultationService;
import ru.fa.legal.service.UserService;

// Импорт классов для работы с коллекциями и датами
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Контроллер для отображения статистики.
 * Обрабатывает запросы на просмотр статистических данных системы.
 * Реализует требование по статистическим функциям из задания.
 *
 * @author Иванов Егор Борисович
 * @version 1.0
 */
@Controller
@RequestMapping("/statistics")
@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')") // Доступ только для менеджеров и админов
public class StatisticsController {

    /**
     * Сервис статистики.
     */
    @Autowired
    private StatisticsService statisticsService;

    /**
     * Сервис дел.
     */
    @Autowired
    private CaseService caseService;

    /**
     * Сервис консультаций.
     */
    @Autowired
    private ConsultationService consultationService;

    /**
     * Сервис пользователей.
     */
    @Autowired
    private UserService userService;

    /**
     * Отображает главную страницу статистики.
     * Показывает общую сводку основных показателей системы.
     *
     * @param model модель для передачи данных
     * @return имя шаблона статистики
     */
    @GetMapping
    public String showStatistics(Model model) {

        // ===== ОБЩАЯ СТАТИСТИКА =====

        // Получаем общую статистику системы
        Map<String, Object> generalStats = statisticsService.getGeneralStatistics();

        // Добавляем общую статистику в модель
        model.addAttribute("generalStats", generalStats);

        // ===== СТАТИСТИКА ПО ПОЛЬЗОВАТЕЛЯМ =====

        // Общее количество пользователей (требование: "общее количество пользователей системы")
        long totalUsers = statisticsService.getTotalUserCount();
        model.addAttribute("totalUsers", totalUsers);

        // Распределение пользователей по ролям
        Map<?, ?> usersByRole = statisticsService.getUserCountByRole();
        model.addAttribute("usersByRole", usersByRole);

        // ===== СТАТИСТИКА ПО ДЕЛАМ =====

        // Общее количество дел
        long totalCases = statisticsService.getTotalCaseCount();
        model.addAttribute("totalCases", totalCases);

        // Распределение дел по статусам
        Map<?, ?> casesByStatus = statisticsService.getCaseCountByStatus();
        model.addAttribute("casesByStatus", casesByStatus);

        // Распределение дел по категориям
        Map<?, ?> casesByCategory = statisticsService.getCaseCountByCategory();
        model.addAttribute("casesByCategory", casesByCategory);

        // Среднее количество дел на юриста (требование: "среднее время ожидания чего-либо")
        double avgCasesPerLawyer = statisticsService.getAverageCasesPerLawyer();
        model.addAttribute("avgCasesPerLawyer", avgCasesPerLawyer);

        // ===== СТАТИСТИКА ПО КОНСУЛЬТАЦИЯМ =====

        // Общее количество консультаций
        long totalConsultations = statisticsService.getTotalConsultationCount();
        model.addAttribute("totalConsultations", totalConsultations);

        // Распределение консультаций по статусам
        Map<?, ?> consultationsByStatus = statisticsService.getConsultationCountByStatus();
        model.addAttribute("consultationsByStatus", consultationsByStatus);

        // Средняя продолжительность консультации
        double avgConsultationDuration = statisticsService.getAverageConsultationDuration();
        model.addAttribute("avgConsultationDuration", avgConsultationDuration);

        // ===== СТАТИСТИКА ПО ДОКУМЕНТАМ =====

        // Общее количество документов
        long totalDocuments = statisticsService.getTotalDocumentCount();
        model.addAttribute("totalDocuments", totalDocuments);

        // Распределение документов по типам
        Map<?, ?> documentsByType = statisticsService.getDocumentCountByType();
        model.addAttribute("documentsByType", documentsByType);

        // ===== ДАННЫЕ ДЛЯ ДИАГРАММ =====
        // (требование: "допускается реализация гистограммы/диаграммы")

        // Данные для диаграммы дел по статусам
        Map<String, Object> caseStatusChartData = statisticsService.getCaseStatusChartData();
        model.addAttribute("caseStatusChartData", caseStatusChartData);

        // Данные для диаграммы дел по категориям
        Map<String, Object> caseCategoryChartData = statisticsService.getCaseCategoryChartData();
        model.addAttribute("caseCategoryChartData", caseCategoryChartData);

        // ===== ТОП ЮРИСТОВ =====

        // Топ-5 юристов по количеству дел
        var topLawyers = statisticsService.getTopLawyersByCaseCount(5);
        model.addAttribute("topLawyers", topLawyers);

        // Возвращаем шаблон statistics.html
        return "statistics";
    }

    /**
     * Отображает статистику за указанный период.
     *
     * @param startDate начальная дата (необязательный параметр)
     * @param endDate конечная дата (необязательный параметр)
     * @param model модель для передачи данных
     * @return имя шаблона статистики за период
     */
    @GetMapping("/period")
    public String showPeriodStatistics(@RequestParam(required = false) String startDate,
                                       @RequestParam(required = false) String endDate,
                                       Model model) {

        // Устанавливаем даты по умолчанию, если не указаны
        LocalDate start;
        LocalDate end;

        // Если даты не указаны, используем текущий месяц
        if (startDate == null || startDate.isEmpty()) {
            start = LocalDate.now().withDayOfMonth(1); // Первый день текущего месяца
        } else {
            start = LocalDate.parse(startDate); // Парсим строку в дату
        }

        if (endDate == null || endDate.isEmpty()) {
            end = LocalDate.now(); // Текущая дата
        } else {
            end = LocalDate.parse(endDate);
        }

        // Получаем статистику по делам за период
        Map<String, Object> caseStats = statisticsService.getCaseStatisticsForPeriod(start, end);
        model.addAttribute("caseStats", caseStats);

        // Получаем статистику по консультациям за период
        LocalDateTime startDateTime = start.atStartOfDay(); // Начало дня
        LocalDateTime endDateTime = end.atTime(23, 59, 59); // Конец дня
        Map<String, Object> consultationStats = statisticsService.getConsultationStatisticsForPeriod(startDateTime, endDateTime);
        model.addAttribute("consultationStats", consultationStats);

        // Добавляем даты в модель для отображения в форме
        model.addAttribute("startDate", start);
        model.addAttribute("endDate", end);

        // Возвращаем шаблон statistics-period.html
        return "statistics-period";
    }

    /**
     * API endpoint для получения данных диаграммы дел по статусам.
     * Возвращает JSON для использования в JavaScript диаграммах.
     *
     * @ResponseBody - возвращает данные напрямую, а не имя шаблона
     * @return данные для диаграммы в формате JSON
     */
    @GetMapping("/api/cases-by-status")
    @ResponseBody
    public Map<String, Object> getCasesByStatusChartData() {
        // Возвращаем данные в формате, подходящем для Chart.js
        return statisticsService.getCaseStatusChartData();
    }

    /**
     * API endpoint для получения данных диаграммы дел по категориям.
     *
     * @return данные для диаграммы в формате JSON
     */
    @GetMapping("/api/cases-by-category")
    @ResponseBody
    public Map<String, Object> getCasesByCategoryChartData() {
        // Возвращаем данные для диаграммы
        return statisticsService.getCaseCategoryChartData();
    }

    /**
     * API endpoint для получения общей статистики.
     *
     * @return общая статистика в формате JSON
     */
    @GetMapping("/api/general")
    @ResponseBody
    public Map<String, Object> getGeneralStatistics() {
        // Возвращаем общую статистику
        return statisticsService.getGeneralStatistics();
    }

    /**
     * Отображает детальную статистику по пользователям.
     *
     * @param model модель для передачи данных
     * @return имя шаблона статистики пользователей
     */
    @GetMapping("/users")
    public String showUserStatistics(Model model) {

        // Общее количество пользователей
        long totalUsers = statisticsService.getTotalUserCount();
        model.addAttribute("totalUsers", totalUsers);

        // Распределение по ролям
        var usersByRole = statisticsService.getUserCountByRole();
        model.addAttribute("usersByRole", usersByRole);

        // Список всех пользователей для детального просмотра
        var allUsers = userService.getAllUsers();
        model.addAttribute("allUsers", allUsers);

        // Возвращаем шаблон
        return "statistics-users";
    }

    /**
     * Отображает детальную статистику по делам.
     *
     * @param model модель для передачи данных
     * @return имя шаблона статистики дел
     */
    @GetMapping("/cases")
    public String showCaseStatistics(Model model) {

        // Общее количество дел
        long totalCases = statisticsService.getTotalCaseCount();
        model.addAttribute("totalCases", totalCases);

        // Распределение по статусам
        var casesByStatus = statisticsService.getCaseCountByStatus();
        model.addAttribute("casesByStatus", casesByStatus);

        // Распределение по категориям
        var casesByCategory = statisticsService.getCaseCountByCategory();
        model.addAttribute("casesByCategory", casesByCategory);

        // Среднее количество дел на юриста
        double avgCasesPerLawyer = statisticsService.getAverageCasesPerLawyer();
        model.addAttribute("avgCasesPerLawyer", avgCasesPerLawyer);

        // Топ юристов
        var topLawyers = statisticsService.getTopLawyersByCaseCount(10);
        model.addAttribute("topLawyers", topLawyers);

        // Данные для диаграмм
        model.addAttribute("caseStatusChartData", statisticsService.getCaseStatusChartData());
        model.addAttribute("caseCategoryChartData", statisticsService.getCaseCategoryChartData());

        // Возвращаем шаблон
        return "statistics-cases";
    }

    /**
     * Отображает детальную статистику по консультациям.
     *
     * @param model модель для передачи данных
     * @return имя шаблона статистики консультаций
     */
    @GetMapping("/consultations")
    public String showConsultationStatistics(Model model) {

        // Общее количество консультаций
        long totalConsultations = statisticsService.getTotalConsultationCount();
        model.addAttribute("totalConsultations", totalConsultations);

        // Распределение по статусам
        var consultationsByStatus = statisticsService.getConsultationCountByStatus();
        model.addAttribute("consultationsByStatus", consultationsByStatus);

        // Средняя продолжительность
        double avgDuration = statisticsService.getAverageConsultationDuration();
        model.addAttribute("avgConsultationDuration", avgDuration);

        // Неоплаченные консультации
        var unpaidConsultations = consultationService.getUnpaidConsultations();
        model.addAttribute("unpaidConsultations", unpaidConsultations);
        model.addAttribute("unpaidCount", unpaidConsultations.size());

        // Возвращаем шаблон
        return "statistics-consultations";
    }
}