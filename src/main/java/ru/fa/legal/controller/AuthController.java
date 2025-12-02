package ru.fa.legal.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.fa.legal.model.User;
import ru.fa.legal.model.UserRole;
import ru.fa.legal.service.UserService;
import jakarta.validation.Valid;

/**
 * Контроллер для управления аутентификацией и регистрацией.
 * Обрабатывает запросы входа, выхода и регистрации пользователей.
 *
 * @author Киселева Ольга Ивановна
 * @version 1.0
 */
@Controller
public class AuthController {

    /**
     * Сервис для работы с пользователями.
     */
    @Autowired
    private UserService userService;

    /**
     * Главная страница приложения.
     *
     * @return имя шаблона главной страницы
     */
    @GetMapping("/")
    public String index() {
        return "index";
    }

    /**
     * Отображает страницу входа в систему.
     *
     * @GetMapping("/login") - обрабатывает GET запрос на /login
     * @param error параметр, указывающий на ошибку входа
     * @param logout параметр, указывающий на успешный выход
     * @param model модель для передачи данных в представление
     * @return имя шаблона страницы входа
     */
    @GetMapping("/login")
    public String showLoginPage(@RequestParam(required = false) String error,
                                @RequestParam(required = false) String logout,
                                Model model) {

        if (error != null) {
            model.addAttribute("error", "Неверное имя пользователя или пароль");
        }

        if (logout != null) {
            model.addAttribute("message", "Вы успешно вышли из системы");
        }

        return "login";
    }

    /**
     * Отображает страницу регистрации.
     *
     * @param model модель для передачи данных
     * @return имя шаблона страницы регистрации
     */
    @GetMapping("/register")
    public String showRegistrationPage(Model model) {
        User user = new User();
        user.setRole(UserRole.CLIENT);
        user.setEnabled(true);
        model.addAttribute("user", user);
        return "register";
    }

    /**
     * Обрабатывает форму регистрации нового пользователя.
     *
     * @PostMapping("/register") - обрабатывает POST запрос на /register
     * @param user данные нового пользователя из формы
     * @param bindingResult результаты валидации
     * @param redirectAttributes атрибуты для передачи сообщений после редиректа
     * @param model модель для передачи данных
     * @return URL для перенаправления или имя шаблона
     */
    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") User user,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes,
                               Model model) {

        if (bindingResult.hasErrors()) {
            return "register";
        }

        if (userService.existsByUsername(user.getUsername())) {
            model.addAttribute("error", "Пользователь с таким именем уже существует");
            return "register";
        }

        if (userService.existsByEmail(user.getEmail())) {
            model.addAttribute("error", "Пользователь с таким email уже существует");
            return "register";
        }

        try {
            userService.createUser(user);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Регистрация прошла успешно! Теперь вы можете войти.");

            return "redirect:/login?registered";

        } catch (Exception e) {
            model.addAttribute("error", "Ошибка при регистрации: " + e.getMessage());
            return "register";
        }
    }

    /**
     * Страница запрета доступа.
     * Отображается, когда пользователь пытается получить доступ к ресурсу,
     * для которого у него нет прав.
     *
     * @return имя шаблона страницы ошибки доступа
     */
    @GetMapping("/access-denied")
    public String accessDenied() {
        return "access-denied";
    }

    /**
     * Главная панель управления после входа.
     * Отображает различную информацию в зависимости от роли пользователя.
     *
     * @param model модель для передачи данных
     * @return имя шаблона панели управления
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // Здесь можно добавить загрузку статистики для дашборда
        // Например:
        // model.addAttribute("totalCases", caseService.getCaseCount());
        // model.addAttribute("activeCases", caseService.getCaseCountByStatus(CaseStatus.IN_PROGRESS));

        return "dashboard";
    }

    /**
     * Страница "Об авторе".
     * Отображает информацию о разработчике проекта.
     *
     * @return имя шаблона страницы "Об авторе"
     */
    @GetMapping("/about")
    public String about() {
        return "about";
    }
}