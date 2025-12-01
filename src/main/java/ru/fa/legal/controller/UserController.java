package ru.fa.legal.controller;

// Импорт Spring MVC аннотаций
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// Импорт моделей и сервисов
import ru.fa.legal.model.User;
import ru.fa.legal.model.UserRole;
import ru.fa.legal.service.UserService;

// Импорт аннотаций валидации
import jakarta.validation.Valid;

// Импорт классов для работы с коллекциями
import java.util.List;

/**
 * Контроллер для управления пользователями.
 * Обрабатывает HTTP запросы, связанные с пользователями.
 * Доступен только администраторам.
 *
 * @author Иванов Егор Борисович
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
        // Получаем всех пользователей через сервис
        List<User> users = userService.getAllUsers();

        // Добавляем список пользователей в модель
        model.addAttribute("listUsers", users);

        // Возвращаем шаблон users/list.html
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

        // Список для хранения результатов
        List<User> users;

        // Если ключевое слово не указано или пустое, показываем всех пользователей
        if (keyword == null || keyword.trim().isEmpty()) {
            users = userService.getAllUsers();
        } else {
            // Иначе выполняем поиск
            // Получаем всех пользователей и фильтруем по ключевому слову
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

        // Добавляем результаты в модель
        model.addAttribute("listUsers", users);
        model.addAttribute("keyword", keyword);

        // Возвращаем тот же шаблон списка
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
        // Создаем пустой объект пользователя для формы
        User user = new User();

        // По умолчанию новый пользователь активен
        user.setEnabled(true);

        // Добавляем объект в модель
        model.addAttribute("user", user);

        // Добавляем список ролей для выпадающего списка
        model.addAttribute("roles", UserRole.values());

        // Возвращаем шаблон users/add.html
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
        // Получаем пользователя по ID
        User user = userService.getUserById(id);

        // Очищаем пароль для безопасности (не показываем в форме)
        user.setPassword("");

        // Добавляем пользователя в модель
        model.addAttribute("user", user);

        // Добавляем список ролей
        model.addAttribute("roles", UserRole.values());

        // Возвращаем шаблон users/edit.html
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

        // Проверяем ошибки валидации
        if (bindingResult.hasErrors()) {
            // Если есть ошибки, возвращаем к форме
            // Определяем, это создание или редактирование
            if (user.getId() == null) {
                return "users/add";
            } else {
                return "users/edit";
            }
        }

        try {
            // Определяем, создание это или обновление
            if (user.getId() == null) {
                // Создание нового пользователя
                userService.createUser(user);
                redirectAttributes.addFlashAttribute("successMessage",
                        "Пользователь '" + user.getUsername() + "' успешно создан");
            } else {
                // Обновление существующего пользователя
                userService.updateUser(user.getId(), user);
                redirectAttributes.addFlashAttribute("successMessage",
                        "Пользователь '" + user.getUsername() + "' успешно обновлен");
            }

            // Перенаправляем на список пользователей
            return "redirect:/users/list";

        } catch (Exception e) {
            // Обрабатываем ошибку
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при сохранении пользователя: " + e.getMessage());

            // Перенаправляем обратно
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
            // Получаем пользователя для отображения имени в сообщении
            User user = userService.getUserById(id);
            String username = user.getUsername();

            // Удаляем пользователя
            userService.deleteUser(id);

            // Добавляем сообщение об успехе
            redirectAttributes.addFlashAttribute("successMessage",
                    "Пользователь '" + username + "' успешно удален");

        } catch (Exception e) {
            // Обрабатываем ошибку
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при удалении пользователя: " + e.getMessage());
        }

        // Перенаправляем на список пользователей
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
        // Получаем пользователя
        User user = userService.getUserById(id);

        // Добавляем пользователя в модель
        model.addAttribute("user", user);

        // Возвращаем шаблон users/view.html
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
            // Изменяем роль пользователя
            User user = userService.changeUserRole(id, role);

            // Добавляем сообщение об успехе
            redirectAttributes.addFlashAttribute("successMessage",
                    "Роль пользователя '" + user.getUsername() + "' изменена на " + role.name());

        } catch (Exception e) {
            // Обрабатываем ошибку
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при изменении роли: " + e.getMessage());
        }

        // Перенаправляем обратно
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
            // Получаем пользователя
            User user = userService.getUserById(id);

            // Переключаем статус
            boolean newStatus = !user.getEnabled();
            userService.toggleUserStatus(id, newStatus);

            // Добавляем сообщение об успехе
            String statusText = newStatus ? "активирован" : "заблокирован";
            redirectAttributes.addFlashAttribute("successMessage",
                    "Пользователь '" + user.getUsername() + "' " + statusText);

        } catch (Exception e) {
            // Обрабатываем ошибку
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при изменении статуса: " + e.getMessage());
        }

        // Перенаправляем обратно
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

        // Список пользователей
        List<User> users;

        // Если роль не указана, показываем всех
        if (role == null) {
            users = userService.getAllUsers();
        } else {
            // Иначе фильтруем по роли
            users = userService.getUsersByRole(role);
        }

        // Добавляем результаты в модель
        model.addAttribute("listUsers", users);
        model.addAttribute("selectedRole", role);
        model.addAttribute("roles", UserRole.values());

        // Возвращаем шаблон списка
        return "users/list";
    }
}