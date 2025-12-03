package ru.fa.legal.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.fa.legal.model.Consultation;
import ru.fa.legal.model.ConsultationStatus;
import ru.fa.legal.model.ConsultationType;
import ru.fa.legal.model.User;
import ru.fa.legal.model.UserRole;
import ru.fa.legal.service.ConsultationService;
import ru.fa.legal.service.UserService;
import jakarta.validation.Valid;
import java.util.List;

/**
 * Контроллер для управления консультациями.
 * Обрабатывает HTTP запросы, связанные с консультациями.
 *
 * @author Киселева Ольга Ивановна
 * @version 1.0
 */
@Controller
@RequestMapping("/consultations")
public class ConsultationController {

    @Autowired
    private ConsultationService consultationService;

    @Autowired
    private UserService userService;

    /**
     * Отображает список всех консультаций.
     *
     * @param model модель для передачи данных
     * @param authentication объект аутентификации
     * @return имя шаблона списка консультаций
     */
    @GetMapping("/list")
    public String listConsultations(Model model, Authentication authentication) {
        String username = authentication.getName();
        User currentUser = userService.getUserByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        List<Consultation> consultations;

        switch (currentUser.getRole()) {
            case CLIENT:
                consultations = consultationService.getConsultationsByClient(currentUser.getId());
                break;

            case LAWYER:
                consultations = consultationService.getConsultationsByLawyer(currentUser.getId());
                break;

            case MANAGER:
            case ADMIN:
                consultations = consultationService.getAllConsultations();
                break;

            default:
                consultations = List.of();
                break;
        }

        model.addAttribute("consultations", consultations);
        model.addAttribute("currentUser", currentUser);

        return "consultations/list";
    }

    /**
     * Отображает форму добавления новой консультации.
     *
     * @param model модель для передачи данных
     * @param authentication объект аутентификации
     * @return имя шаблона формы добавления
     */
    @GetMapping("/add")
    public String showAddForm(Model model, Authentication authentication) {
        String username = authentication.getName();
        User currentUser = userService.getUserByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        Consultation consultation = new Consultation();

        if (currentUser.getRole() == UserRole.CLIENT) {
            consultation.setClient(currentUser);
        }

        model.addAttribute("consultation", consultation);
        model.addAttribute("consultationTypes", ConsultationType.values());
        model.addAttribute("consultationStatuses", ConsultationStatus.values());
        model.addAttribute("clients", userService.getUsersByRole(UserRole.CLIENT));
        model.addAttribute("lawyers", userService.getActiveLawyers());
        model.addAttribute("currentUser", currentUser);

        return "consultations/add";
    }

    /**
     * Обрабатывает форму добавления новой консультации.
     *
     * @param consultation данные консультации из формы
     * @param bindingResult результаты валидации
     * @param redirectAttributes атрибуты для сообщений
     * @return URL для перенаправления
     */
    @PostMapping("/add")
    public String addConsultation(@Valid @ModelAttribute("consultation") Consultation consultation,
                                  BindingResult bindingResult,
                                  RedirectAttributes redirectAttributes) {if (bindingResult.hasErrors()) {
        return "consultations/add";
    }

        try {
            Consultation savedConsultation = consultationService.createConsultation(consultation);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Консультация успешно создана на " +
                            savedConsultation.getConsultationDate());

            return "redirect:/consultations/list";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при создании консультации: " + e.getMessage());

            return "redirect:/consultations/add";
        }
    }

    /**
     * Отображает форму редактирования консультации.
     *
     * @param id идентификатор консультации
     * @param model модель для передачи данных
     * @return имя шаблона формы редактирования
     */
    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAnyRole('LAWYER', 'MANAGER', 'ADMIN')")
    public String showEditForm(@PathVariable Long id, Model model) {
        Consultation consultation = consultationService.getConsultationById(id);

        model.addAttribute("consultation", consultation);
        model.addAttribute("consultationTypes", ConsultationType.values());
        model.addAttribute("consultationStatuses", ConsultationStatus.values());
        model.addAttribute("clients", userService.getUsersByRole(UserRole.CLIENT));
        model.addAttribute("lawyers", userService.getActiveLawyers());

        return "consultations/edit";
    }

    /**
     * Обрабатывает форму редактирования консультации.
     *
     * @param id идентификатор консультации
     * @param consultation обновленные данные
     * @param bindingResult результаты валидации
     * @param redirectAttributes атрибуты для сообщений
     * @return URL для перенаправления
     */
    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAnyRole('LAWYER', 'MANAGER', 'ADMIN')")
    public String updateConsultation(@PathVariable Long id,
                                     @Valid @ModelAttribute("consultation") Consultation consultation,
                                     BindingResult bindingResult,
                                     RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return "consultations/edit";
        }

        try {
            Consultation updatedConsultation = consultationService.updateConsultation(id, consultation);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Консультация успешно обновлена");

            return "redirect:/consultations/view/" + id;

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при обновлении консультации: " + e.getMessage());

            return "redirect:/consultations/edit/" + id;
        }
    }

    /**
     * Удаляет консультацию.
     *
     * @param id идентификатор консультации
     * @param redirectAttributes атрибуты для сообщений
     * @return URL для перенаправления
     */
    @PostMapping("/delete/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public String deleteConsultation(@PathVariable Long id,
                                     RedirectAttributes redirectAttributes) {
        try {
            consultationService.deleteConsultation(id);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Консультация успешно удалена");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при удалении консультации: " + e.getMessage());
        }

        return "redirect:/consultations/list";
    }

    /**
     * Отображает подробную информацию о консультации.
     *
     * @param id идентификатор консультации
     * @param model модель для передачи данных
     * @param authentication объект аутентификации
     * @return имя шаблона для отображения
     */
    @GetMapping("/view/{id}")
    public String viewConsultation(@PathVariable Long id,
                                   Model model,
                                   Authentication authentication) {
        Consultation consultation = consultationService.getConsultationById(id);

        String username = authentication.getName();
        User currentUser = userService.getUserByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        // Проверяем права доступа для клиентов
        if (currentUser.getRole() == UserRole.CLIENT &&
                !consultation.getClient().getId().equals(currentUser.getId())) {
            return "redirect:/access-denied";
        }

        model.addAttribute("consultation", consultation);
        model.addAttribute("currentUser", currentUser);

        return "consultations/view";
    }

    /**
     * Изменяет статус консультации.
     *
     * @param id идентификатор консультации
     * @param status новый статус
     * @param redirectAttributes атрибуты для сообщений
     * @return URL для перенаправления
     */
    @PostMapping("/change-status/{id}")
    @PreAuthorize("hasAnyRole('LAWYER', 'MANAGER', 'ADMIN')")
    public String changeConsultationStatus(@PathVariable Long id,
                                           @RequestParam ConsultationStatus status,
                                           RedirectAttributes redirectAttributes) {
        try {
            consultationService.changeConsultationStatus(id, status);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Статус консультации изменен на '" + status.getDisplayName() + "'");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при изменении статуса: " + e.getMessage());
        }

        return "redirect:/consultations/view/" + id;
    }

    /**
     * Отмечает консультацию как оплаченную.
     *
     * @param id идентификатор консультации
     * @param redirectAttributes атрибуты для сообщений
     * @return URL для перенаправления
     */
    @PostMapping("/mark-paid/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public String markAsPaid(@PathVariable Long id,
                             RedirectAttributes redirectAttributes) {
        try {
            consultationService.markAsPaid(id);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Консультация отмечена как оплаченная");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка: " + e.getMessage());
        }

        return "redirect:/consultations/view/" + id;
    }
}