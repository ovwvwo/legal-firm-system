package ru.fa.legal.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.fa.legal.service.StatisticsService;
import ru.fa.legal.service.ConsultationService;
import ru.fa.legal.service.UserService;
import ru.fa.legal.service.CaseService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Контроллер для отображения статистики.
 * Обрабатывает запросы на просмотр статистических данных системы.
 * Реализует требование по статистическим функциям из задания.
 *
 * @author Киселева Ольга Ивановна
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
        Map<String, Object> generalStats = statisticsService.getGeneralStatistics();
        model.addAttribute("generalStats", generalStats);
        long totalUsers = statisticsService.getTotalUserCount();
        model.addAttribute("totalUsers", totalUsers);
        Map<?, ?> usersByRole = statisticsService.getUserCountByRole();
        model.addAttribute("usersByRole", usersByRole);
        long totalCases = statisticsService.getTotalCaseCount();
        model.addAttribute("totalCases", totalCases);
        Map<?, ?> casesByStatus = statisticsService.getCaseCountByStatus();
        model.addAttribute("casesByStatus", casesByStatus);
        Map<?, ?> casesByCategory = statisticsService.getCaseCountByCategory();
        model.addAttribute("casesByCategory", casesByCategory);
        double avgCasesPerLawyer = statisticsService.getAverageCasesPerLawyer();
        model.addAttribute("avgCasesPerLawyer", avgCasesPerLawyer);
        long totalConsultations = statisticsService.getTotalConsultationCount();
        model.addAttribute("totalConsultations", totalConsultations);
        Map<?, ?> consultationsByStatus = statisticsService.getConsultationCountByStatus();
        model.addAttribute("consultationsByStatus", consultationsByStatus);
        double avgConsultationDuration = statisticsService.getAverageConsultationDuration();
        model.addAttribute("avgConsultationDuration", avgConsultationDuration);
        long totalDocuments = statisticsService.getTotalDocumentCount();
        model.addAttribute("totalDocuments", totalDocuments);
        Map<?, ?> documentsByType = statisticsService.getDocumentCountByType();
        model.addAttribute("documentsByType", documentsByType);
        Map<String, Object> caseStatusChartData = statisticsService.getCaseStatusChartData();
        model.addAttribute("caseStatusChartData", caseStatusChartData);
        Map<String, Object> caseCategoryChartData = statisticsService.getCaseCategoryChartData();
        model.addAttribute("caseCategoryChartData", caseCategoryChartData);

        var topLawyers = statisticsService.getTopLawyersByCaseCount(5);
        model.addAttribute("topLawyers", topLawyers);
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
        LocalDate start;
        LocalDate end;

        if (startDate == null || startDate.isEmpty()) {
            start = LocalDate.now().withDayOfMonth(1);
        } else {
            start = LocalDate.parse(startDate);
        }

        if (endDate == null || endDate.isEmpty()) {
            end = LocalDate.now();
        } else {
            end = LocalDate.parse(endDate);
        }
        Map<String, Object> caseStats = statisticsService.getCaseStatisticsForPeriod(start, end);
        model.addAttribute("caseStats", caseStats);

        LocalDateTime startDateTime = start.atStartOfDay(); // Начало дня
        LocalDateTime endDateTime = end.atTime(23, 59, 59); // Конец дня
        Map<String, Object> consultationStats = statisticsService.getConsultationStatisticsForPeriod(startDateTime, endDateTime);
        model.addAttribute("consultationStats", consultationStats);

        model.addAttribute("startDate", start);
        model.addAttribute("endDate", end);
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
        long totalUsers = statisticsService.getTotalUserCount();
        model.addAttribute("totalUsers", totalUsers);
        var usersByRole = statisticsService.getUserCountByRole();
        model.addAttribute("usersByRole", usersByRole);
        var allUsers = userService.getAllUsers();
        model.addAttribute("allUsers", allUsers);
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
        long totalCases = statisticsService.getTotalCaseCount();
        model.addAttribute("totalCases", totalCases);

        var casesByStatus = statisticsService.getCaseCountByStatus();
        model.addAttribute("casesByStatus", casesByStatus);

        var casesByCategory = statisticsService.getCaseCountByCategory();
        model.addAttribute("casesByCategory", casesByCategory);

        double avgCasesPerLawyer = statisticsService.getAverageCasesPerLawyer();
        model.addAttribute("avgCasesPerLawyer", avgCasesPerLawyer);

        var topLawyers = statisticsService.getTopLawyersByCaseCount(10);
        model.addAttribute("topLawyers", topLawyers);

        model.addAttribute("caseStatusChartData", statisticsService.getCaseStatusChartData());
        model.addAttribute("caseCategoryChartData", statisticsService.getCaseCategoryChartData());

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
        long totalConsultations = statisticsService.getTotalConsultationCount();
        model.addAttribute("totalConsultations", totalConsultations);

        var consultationsByStatus = statisticsService.getConsultationCountByStatus();
        model.addAttribute("consultationsByStatus", consultationsByStatus);

        double avgDuration = statisticsService.getAverageConsultationDuration();
        model.addAttribute("avgConsultationDuration", avgDuration);

        var unpaidConsultations = consultationService.getUnpaidConsultations();
        model.addAttribute("unpaidConsultations", unpaidConsultations);
        model.addAttribute("unpaidCount", unpaidConsultations.size());
        return "statistics-consultations";
    }
}