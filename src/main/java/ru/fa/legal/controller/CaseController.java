package ru.fa.legal.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.fa.legal.model.Case;
import ru.fa.legal.model.CaseStatus;
import ru.fa.legal.service.CaseService;
import ru.fa.legal.model.CaseCategory;
import ru.fa.legal.model.Priority;
import ru.fa.legal.model.User;
import ru.fa.legal.service.UserService;
import jakarta.validation.Valid;
import java.util.List;

/**
 * Контроллер для управления делами.
 * Обрабатывает HTTP запросы, связанные с делами.
 * Реализует слой представления в архитектуре MVC.
 *
 * @Controller - помечает класс как Spring MVC контроллер
 * @RequestMapping - базовый URL для всех методов контроллера
 *
 * @author Киселева Ольга Ивановна
 * @version 1.0
 */
@Controller
@RequestMapping("/cases")
public class CaseController {

    /**
     * Сервис для работы с делами.
     */
    @Autowired
    private CaseService caseService;

    /**
     * Сервис для работы с пользователями.
     */
    @Autowired
    private UserService userService;

    /**
     * Отображает список всех дел.
     * Доступно всем аутентифицированным пользователям.
     *
     * @GetMapping - обрабатывает GET запросы на указанный URL
     * @param model модель для передачи данных в представление
     * @param authentication объект аутентификации текущего пользователя
     * @return имя Thymeleaf шаблона для отображения
     */
    @GetMapping("/list")
    public String listCases(Model model, Authentication authentication) {
        String username = authentication.getName();
        User currentUser = userService.getUserByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        List<Case> cases;

        switch (currentUser.getRole()) {
            case CLIENT:
                // Клиент видит только свои дела
                cases = caseService.getCasesByClient(currentUser.getId());
                break;

            case LAWYER:
                cases = caseService.getCasesByLawyer(currentUser.getId());
                break;

            case MANAGER:
            case ADMIN:
                cases = caseService.getAllCases();
                break;

            default:
                cases = List.of();
                break;
        }

        model.addAttribute("cases", cases);
        model.addAttribute("currentUser", currentUser);
        return "cases/list";
    }

    /**
     * Отображает форму для добавления нового дела.
     * Доступно только юристам, менеджерам и администраторам.
     *
     * @PreAuthorize - проверяет права доступа перед выполнением метода
     * @param model модель для передачи данных в представление
     * @return имя шаблона формы добавления
     */
    @GetMapping("/add")
    @PreAuthorize("hasAnyRole('LAWYER', 'MANAGER', 'ADMIN')")
    public String showAddForm(Model model) {
        model.addAttribute("caseEntity", new Case());

        model.addAttribute("categories", CaseCategory.values());
        model.addAttribute("statuses", CaseStatus.values());
        model.addAttribute("priorities", Priority.values());

        model.addAttribute("clients", userService.getUsersByRole(ru.fa.legal.model.UserRole.CLIENT));

        model.addAttribute("lawyers", userService.getActiveLawyers());
        return "cases/add";
    }

    /**
     * Обрабатывает форму добавления нового дела.
     *
     * @PostMapping - обрабатывает POST запросы
     * @Valid - включает валидацию объекта на основе аннотаций в модели
     * @ModelAttribute - связывает данные формы с объектом Java
     * @param caseEntity объект дела из формы
     * @param bindingResult результаты валидации
     * @param redirectAttributes атрибуты для передачи сообщений после редиректа
     * @return URL для перенаправления
     */
    @PostMapping("/add")
    @PreAuthorize("hasAnyRole('LAWYER', 'MANAGER', 'ADMIN')")
    public String addCase(@Valid @ModelAttribute("caseEntity") Case caseEntity,
                          BindingResult bindingResult,
                          RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {return "cases/add";}

        try {
            Case savedCase = caseService.createCase(caseEntity);
            // addFlashAttribute - атрибут сохраняется только для следующего запроса
            redirectAttributes.addFlashAttribute("successMessage",
                    "Дело '" + savedCase.getTitle() + "' успешно создано");
            return "redirect:/cases/list";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при создании дела: " + e.getMessage());
            return "redirect:/cases/add";
        }
    }

    /**
     * Отображает форму редактирования дела.
     *
     * @PathVariable - извлекает значение из URL
     * @param id идентификатор дела из URL
     * @param model модель для передачи данных
     * @return имя шаблона формы редактирования
     */
    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAnyRole('LAWYER', 'MANAGER', 'ADMIN')")
    public String showEditForm(@PathVariable Long id, Model model) {
        Case caseEntity = caseService.getCaseById(id);

        model.addAttribute("caseEntity", caseEntity);

        model.addAttribute("categories", CaseCategory.values());
        model.addAttribute("statuses", CaseStatus.values());
        model.addAttribute("priorities", Priority.values());
        model.addAttribute("clients", userService.getUsersByRole(ru.fa.legal.model.UserRole.CLIENT));
        model.addAttribute("lawyers", userService.getActiveLawyers());
        return "cases/edit";
    }

    /**
     * Обрабатывает форму редактирования дела.
     *
     * @param id идентификатор дела
     * @param caseEntity обновленные данные дела
     * @param bindingResult результаты валидации
     * @param redirectAttributes атрибуты для сообщений
     * @return URL для перенаправления
     */
    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAnyRole('LAWYER', 'MANAGER', 'ADMIN')")
    public String updateCase(@PathVariable Long id,
                             @Valid @ModelAttribute("caseEntity") Case caseEntity,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return "cases/edit";
        }

        try {
            Case updatedCase = caseService.updateCase(id, caseEntity);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Дело '" + updatedCase.getTitle() + "' успешно обновлено");

            return "redirect:/cases/view/" + id;

        } catch (Exception e) {

            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при обновлении дела: " + e.getMessage());

            return "redirect:/cases/edit/" + id;
        }
    }

    /**
     * Удаляет дело.
     * Доступно только менеджерам и администраторам.
     *
     * @param id идентификатор дела
     * @param redirectAttributes атрибуты для сообщений
     * @return URL для перенаправления
     */
    @PostMapping("/delete/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public String deleteCase(@PathVariable Long id,
                             RedirectAttributes redirectAttributes) {
        try {
            caseService.deleteCase(id);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Дело успешно удалено");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при удалении дела: " + e.getMessage());
        }

        return "redirect:/cases/list";
    }

    /**
     * Отображает подробную информацию о деле.
     *
     * @param id идентификатор дела
     * @param model модель для передачи данных
     * @param authentication объект аутентификации
     * @return имя шаблона для отображения
     */
    @GetMapping("/view/{id}")
    public String viewCase(@PathVariable Long id,
                           Model model,
                           Authentication authentication) {
        Case caseEntity = caseService.getCaseById(id);

        String username = authentication.getName();
        User currentUser = userService.getUserByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        if (currentUser.getRole() == ru.fa.legal.model.UserRole.CLIENT &&
                !caseEntity.getClient().getId().equals(currentUser.getId())) {
            return "redirect:/access-denied";
        }

        model.addAttribute("caseEntity", caseEntity);
        model.addAttribute("currentUser", currentUser);
        return "cases/view";
    }

    /**
     * Поиск дел по различным критериям.
     *
     * @RequestParam - извлекает параметры из query string
     * required = false - параметр не обязателен
     * @param keyword ключевое слово для поиска
     * @param status статус дела
     * @param category категория дела
     * @param model модель для передачи данных
     * @return имя шаблона со результатами поиска
     */
    @GetMapping("/search")
    public String searchCases(@RequestParam(required = false) String keyword,
                              @RequestParam(required = false) CaseStatus status,
                              @RequestParam(required = false) CaseCategory category,
                              Model model) {
        List<Case> cases;

        if (keyword != null && !keyword.isEmpty()) {
            // Поиск по ключевому слову (будет реализован в сервисе)
            cases = caseService.searchCasesByKeyword(keyword);
        } else if (status != null) {
            // Фильтрация по статусу
            cases = caseService.getCasesByStatus(status);
        } else if (category != null) {
            // Фильтрация по категории
            cases = caseService.getCasesByCategory(category);
        } else {
            // Если параметры не заданы, показываем все дела
            cases = caseService.getAllCases();
        }

        model.addAttribute("cases", cases);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedCategory", category);

        model.addAttribute("statuses", CaseStatus.values());
        model.addAttribute("categories", CaseCategory.values());
        return "cases/search";
    }
}