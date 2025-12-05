package ru.fa.legal.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.fa.legal.service.CaseService;
import ru.fa.legal.service.ConsultationService;
import ru.fa.legal.service.DocumentService;
import ru.fa.legal.service.UserService;
import ru.fa.legal.model.User;
import ru.fa.legal.model.Case;
import ru.fa.legal.model.Consultation;
import java.util.List;

/**
 * Контроллер панели управления
 */
@Controller
public class DashboardController {

    @Autowired
    private CaseService caseService;

    @Autowired
    private ConsultationService consultationService;

    @Autowired
    private DocumentService documentService;

    @Autowired
    private UserService userService;

    // @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        try {
            // Получаем текущего пользователя
            String username = authentication.getName();
            User currentUser = userService.getUserByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            // Базовая статистика
            long totalCases = caseService.getCaseCount();
            model.addAttribute("totalCases", totalCases);

            // Активные дела (статус IN_PROGRESS)
            long activeCases = caseService.getCaseCountByStatus(ru.fa.legal.model.CaseStatus.IN_PROGRESS);
            model.addAttribute("activeCases", activeCases);

            // Консультации
            long totalConsultations = consultationService.getConsultationCount();
            model.addAttribute("totalConsultations", totalConsultations);

            // Документы
            long totalDocuments = documentService.getAllDocuments().size();
            model.addAttribute("totalDocuments", totalDocuments);

            // Последние дела (с проверкой на null)
            try {
                List<Case> recentCases = caseService.getRecentCases(5);
                model.addAttribute("recentCases", recentCases != null ? recentCases : List.of());
            } catch (Exception e) {
                model.addAttribute("recentCases", List.of());
            }

            // Предстоящие консультации (с проверкой на null)
            try {
                List<Consultation> upcomingConsultations = consultationService.getUpcomingConsultations(5);
                model.addAttribute("upcomingConsultations", upcomingConsultations != null ? upcomingConsultations : List.of());
            } catch (Exception e) {
                model.addAttribute("upcomingConsultations", List.of());
            }

            model.addAttribute("currentUser", currentUser);

            return "dashboard";
        } catch (Exception e) {
            // Логируем ошибку
            System.err.println("Ошибка в dashboard: " + e.getMessage());
            e.printStackTrace();

            // Добавляем информацию об ошибке в модель
            model.addAttribute("errorMessage", "Произошла ошибка при загрузке панели управления: " + e.getMessage());

            // Возвращаем шаблон с базовыми данными
            model.addAttribute("totalCases", 0);
            model.addAttribute("activeCases", 0);
            model.addAttribute("totalConsultations", 0);
            model.addAttribute("totalDocuments", 0);
            model.addAttribute("recentCases", List.of());
            model.addAttribute("upcomingConsultations", List.of());

            return "dashboard";
        }
    }
}