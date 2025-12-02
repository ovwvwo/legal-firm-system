package ru.fa.legal.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.fa.legal.model.User;
import ru.fa.legal.model.UserRole;
import ru.fa.legal.service.UserService;
import jakarta.validation.Valid;
import java.util.List;

/**
 * Контроллер для управления пользователями.
 * Обрабатывает HTTP запросы, связанные с пользователями.
 * Доступен только администраторам.
 *
 * @author Киселева Ольга Ивановна
 * @version 1.0
 */
@Controller
@RequestMapping("/users")
@PreAuthorize("hasRole('ADMIN')") // Доступ только для администраторов
public class UserController {

    /**
     * Сервис для работы с пользователями.
     */
    @Autowired
    private UserService userService;

    /**
     * Отображает список всех пользователей.
     *
     * @param model модель для передачи данных
     * @return имя шаблона со списком пользователей
     */
    @GetMapping("/list")
    public String listUsers(Model model) {
        List<User> users = userService.getAllUsers();
        model.addAttribute("listUsers", users);
        return "users/list";
    }

    /**
     * Поиск пользователей по ключевому слову.
     *
     * @param keyword ключевое слово для поиска
     * @param model модель для передачи данных
     * @return имя шаблона со списком пользователей
     */
    @GetMapping("/search")
    public String searchUsers(@RequestParam(required = false) String keyword, Model model) {
        List<User> users;

        // Если ключевое слово не указано или пустое, показываем всех пользователей
        if (keyword == null || keyword.trim().isEmpty()) {
            users = userService.getAllUsers();
        } else {

            String lowerKeyword = keyword.toLowerCase();
            users = userService.getAllUsers().stream()
                    .filter(u ->
                            u.getUsername().toLowerCase().contains(lowerKeyword) ||
                                    u.getEmail().toLowerCase().contains(lowerKeyword) ||
                                    u.getFullName().toLowerCase().contains(lowerKeyword) ||
                                    (u.getPhoneNumber() != null && u.getPhoneNumber().contains(keyword)) ||
                                    u.getRole().name().toLowerCase().contains(lowerKeyword)
                    )
                    .toList();
        }

        model.addAttribute("listUsers", users);
        model.addAttribute("keyword", keyword);
        return "users/list";
    }

    /**
     * Отображает форму для добавления нового пользователя.
     *
     * @param model модель для передачи данных
     * @return имя шаблона формы добавления
     */
    @GetMapping("/add")
    public String showAddForm(Model model) {
        User user = new User();
        user.setEnabled(true);
        model.addAttribute("user", user);
        model.addAttribute("roles", UserRole.values());
        return "users/add";
    }

    /**
     * Отображает форму редактирования пользователя.
     *
     * @param id идентификатор пользователя
     * @param model модель для передачи данных
     * @return имя шаблона формы редактирования
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        User user = userService.getUserById(id);

        // Очищаем пароль для безопасности (не показываем в форме)
        user.setPassword("");
        model.addAttribute("user", user);
        model.addAttribute("roles", UserRole.values());
        return "users/edit";
    }

    /**
     * Сохраняет пользователя (создание или обновление).
     * Используется для обработки форм добавления и редактирования.
     *
     * @param user данные пользователя из формы
     * @param bindingResult результаты валидации
     * @param redirectAttributes атрибуты для сообщений
     * @return URL для перенаправления
     */
    @PostMapping("/save")
    public String saveUser(@Valid @ModelAttribute("user") User user,
                           BindingResult bindingResult,
                           RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            if (user.getId() == null) {
                return "users/add";
            } else {
                return "users/edit";
            }
        }

        try {
            if (user.getId() == null) {
                userService.createUser(user);
                redirectAttributes.addFlashAttribute("successMessage",
                        "Пользователь '" + user.getUsername() + "' успешно создан");
            } else {
                userService.updateUser(user.getId(), user);
                redirectAttributes.addFlashAttribute("successMessage",
                        "Пользователь '" + user.getUsername() + "' успешно обновлен");
            }
            return "redirect:/users/list";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при сохранении пользователя: " + e.getMessage());

            if (user.getId() == null) {
                return "redirect:/users/add";
            } else {
                return "redirect:/users/edit/" + user.getId();
            }
        }
    }

    /**
     * Удаляет пользователя.
     *
     * @param id идентификатор пользователя
     * @param redirectAttributes атрибуты для сообщений
     * @return URL для перенаправления
     */
    @PostMapping("/delete/{id}")
    public String deleteUser(@PathVariable Long id,
                             RedirectAttributes redirectAttributes) {
        try {
            User user = userService.getUserById(id);
            String username = user.getUsername();
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Пользователь '" + username + "' успешно удален");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при удалении пользователя: " + e.getMessage());
        }
        return "redirect:/users/list";
    }

    /**
     * Отображает подробную информацию о пользователе.
     *
     * @param id идентификатор пользователя
     * @param model модель для передачи данных
     * @return имя шаблона для отображения
     */
    @GetMapping("/view/{id}")
    public String viewUser(@PathVariable Long id, Model model) {
        User user = userService.getUserById(id);
        model.addAttribute("user", user);
        return "users/view";
    }

    /**
     * Изменяет роль пользователя.
     *
     * @param id идентификатор пользователя
     * @param role новая роль
     * @param redirectAttributes атрибуты для сообщений
     * @return URL для перенаправления
     */
    @PostMapping("/change-role/{id}")
    public String changeUserRole(@PathVariable Long id,
                                 @RequestParam UserRole role,
                                 RedirectAttributes redirectAttributes) {
        try {
            User user = userService.changeUserRole(id, role);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Роль пользователя '" + user.getUsername() + "' изменена на " + role.name());

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при изменении роли: " + e.getMessage());
        }
        return "redirect:/users/view/" + id;
    }

    /**
     * Переключает статус активности пользователя (активен/заблокирован).
     *
     * @param id идентификатор пользователя
     * @param redirectAttributes атрибуты для сообщений
     * @return URL для перенаправления
     */
    @PostMapping("/toggle-status/{id}")
    public String toggleUserStatus(@PathVariable Long id,
                                   RedirectAttributes redirectAttributes) {
        try {
            User user = userService.getUserById(id);

            boolean newStatus = !user.getEnabled();
            userService.toggleUserStatus(id, newStatus);

            String statusText = newStatus ? "активирован" : "заблокирован";
            redirectAttributes.addFlashAttribute("successMessage",
                    "Пользователь '" + user.getUsername() + "' " + statusText);

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при изменении статуса: " + e.getMessage());
        }
        return "redirect:/users/view/" + id;
    }

    /**
     * Фильтрация пользователей по роли.
     *
     * @param role роль для фильтрации
     * @param model модель для передачи данных
     * @return имя шаблона со списком пользователей
     */
    @GetMapping("/filter")
    public String filterUsersByRole(@RequestParam(required = false) UserRole role, Model model) {
        List<User> users;
        if (role == null) {
            users = userService.getAllUsers();
        } else {
            users = userService.getUsersByRole(role);
        }

        model.addAttribute("listUsers", users);
        model.addAttribute("selectedRole", role);
        model.addAttribute("roles", UserRole.values());
        return "users/list";
    }
}