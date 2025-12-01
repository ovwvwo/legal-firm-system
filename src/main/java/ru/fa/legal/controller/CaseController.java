package ru.fa.legal.controller;

// Импорт Spring MVC аннотаций
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// Импорт моделей и сервисов
import ru.fa.legal.model.Case;
import ru.fa.legal.model.CaseStatus;
import ru.fa.legal.model.CaseCategory;
import ru.fa.legal.model.Priority;
import ru.fa.legal.model.User;
import ru.fa.legal.service.UserService;

// Импорт аннотаций валидации
import jakarta.validation.Valid;

// Импорт классов для работы с коллекциями
import java.util.List;

/**
 * Контроллер для управления делами.
 * Обрабатывает HTTP запросы, связанные с делами.
 * Реализует слой представления в архитектуре MVC.
 *
 * @Controller - помечает класс как Spring MVC контроллер
 * @RequestMapping - базовый URL для всех методов контроллера
 *
 * @author Иванов Егор Борисович
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
        // Получаем имя текущего пользователя из объекта аутентификации
        String username = authentication.getName();

        // Получаем пользователя из базы данных
        User currentUser = userService.getUserByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        // Список дел зависит от роли пользователя
        List<Case> cases;

        // Используем switch для определения, какие дела показывать
        switch (currentUser.getRole()) {
            case CLIENT:
                // Клиент видит только свои дела
                cases = caseService.getCasesByClient(currentUser.getId());
                break;

            case LAWYER:
                // Юрист видит дела, которые ему назначены
                cases = caseService.getCasesByLawyer(currentUser.getId());
                break;

            case MANAGER:
            case ADMIN:
                // Менеджер и администратор видят все дела
                cases = caseService.getAllCases();
                break;

            default:
                // По умолчанию пустой список
                cases = List.of();
                break;
        }

        // Добавляем список дел в модель для отображения в шаблоне
        model.addAttribute("cases", cases);
        // Добавляем информацию о текущем пользователе
        model.addAttribute("currentUser", currentUser);

        // Возвращаем имя шаблона (cases/list.html)
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
        // Создаем пустой объект дела для формы
        model.addAttribute("caseEntity", new Case());

        // Добавляем списки для выпадающих списков в форме
        // values() - возвращает массив всех значений enum
        model.addAttribute("categories", CaseCategory.values());
        model.addAttribute("statuses", CaseStatus.values());
        model.addAttribute("priorities", Priority.values());

        // Получаем список клиентов для выбора
        model.addAttribute("clients", userService.getUsersByRole(ru.fa.legal.model.UserRole.CLIENT));

        // Получаем список активных юристов для назначения
        model.addAttribute("lawyers", userService.getActiveLawyers());

        // Возвращаем имя шаблона формы
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

        // Проверяем результаты валидации
        // hasErrors() - возвращает true, если есть ошибки валидации
        if (bindingResult.hasErrors()) {
            // Если есть ошибки, возвращаем пользователя к форме
            return "cases/add";
        }

        // Обработка исключений при сохранении
        try {
            // Сохраняем дело через сервис
            Case savedCase = caseService.createCase(caseEntity);

            // Добавляем сообщение об успехе
            // addFlashAttribute - атрибут сохраняется только для следующего запроса
            redirectAttributes.addFlashAttribute("successMessage",
                    "Дело '" + savedCase.getTitle() + "' успешно создано");

            // Перенаправляем на список дел
            // redirect: - префикс для перенаправления
            return "redirect:/cases/list";

        } catch (Exception e) {
            // В случае ошибки добавляем сообщение об ошибке
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при создании дела: " + e.getMessage());

            // Перенаправляем обратно на форму
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
        // Получаем дело по ID
        Case caseEntity = caseService.getCaseById(id);

        // Добавляем дело в модель
        model.addAttribute("caseEntity", caseEntity);

        // Добавляем справочные данные
        model.addAttribute("categories", CaseCategory.values());
        model.addAttribute("statuses", CaseStatus.values());
        model.addAttribute("priorities", Priority.values());
        model.addAttribute("clients", userService.getUsersByRole(ru.fa.legal.model.UserRole.CLIENT));
        model.addAttribute("lawyers", userService.getActiveLawyers());

        // Возвращаем шаблон формы редактирования
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

        // Проверяем ошибки валидации
        if (bindingResult.hasErrors()) {
            return "cases/edit";
        }

        try {
            // Обновляем дело через сервис
            Case updatedCase = caseService.updateCase(id, caseEntity);

            // Добавляем сообщение об успехе
            redirectAttributes.addFlashAttribute("successMessage",
                    "Дело '" + updatedCase.getTitle() + "' успешно обновлено");

            // Перенаправляем на просмотр дела
            return "redirect:/cases/view/" + id;

        } catch (Exception e) {
            // Обрабатываем ошибку
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
            // Удаляем дело через сервис
            caseService.deleteCase(id);

            // Добавляем сообщение об успехе
            redirectAttributes.addFlashAttribute("successMessage",
                    "Дело успешно удалено");

        } catch (Exception e) {
            // Обрабатываем ошибку
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при удалении дела: " + e.getMessage());
        }

        // Перенаправляем на список дел
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
        // Получаем дело
        Case caseEntity = caseService.getCaseById(id);

        // Получаем текущего пользователя
        String username = authentication.getName();
        User currentUser = userService.getUserByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        // Проверяем права доступа для клиентов
        // Клиент может видеть только свои дела
        if (currentUser.getRole() == ru.fa.legal.model.UserRole.CLIENT &&
                !caseEntity.getClient().getId().equals(currentUser.getId())) {
            // Если клиент пытается просмотреть чужое дело, перенаправляем на страницу ошибки
            return "redirect:/access-denied";
        }

        // Добавляем дело в модель
        model.addAttribute("caseEntity", caseEntity);
        model.addAttribute("currentUser", currentUser);

        // Возвращаем шаблон просмотра
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

        // Список для хранения результатов поиска
        List<Case> cases;

        // Логика поиска в зависимости от заполненных параметров
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

        // Добавляем результаты в модель
        model.addAttribute("cases", cases);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedCategory", category);

        // Добавляем справочники для фильтров
        model.addAttribute("statuses", CaseStatus.values());
        model.addAttribute("categories", CaseCategory.values());

        // Возвращаем шаблон с результатами
        return "cases/search";
    }
}