package ru.fa.legal.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.fa.legal.model.User;
import ru.fa.legal.model.UserRole;
import ru.fa.legal.repository.UserRepository;
import java.util.List;
import java.util.Optional;

/**
 * Сервис для работы с пользователями.
 * Содержит бизнес-логику управления пользователями.
 * Реализует слой сервера в трехзвенной архитектуре.
 *
 * @author Киселева Ольга Ивановна
 * @version 1.0
 */
// @Service - помечает класс как сервисный компонент Spring
@Service
// @Transactional - все методы выполняются в транзакциях
// Если метод завершится с ошибкой, все изменения будут отменены
@Transactional
public class UserService {

    /**
     * Репозиторий для доступа к данным пользователей.
     */
    // @Autowired - автоматическое внедрение зависимости Spring
    @Autowired
    private UserRepository userRepository;

    /**
     * Кодировщик паролей для безопасного хранения.
     * Использует bcrypt алгоритм для хеширования паролей.
     */
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Получить всех пользователей.
     *
     * @return список всех пользователей
     */
    // @Transactional(readOnly = true) - оптимизация для операций только чтения
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        // Вызываем метод findAll() репозитория для получения всех записей
        return userRepository.findAll();
    }

    /**
     * Получить пользователя по ID.
     *
     * @param id идентификатор пользователя
     * @return пользователь
     * @throws RuntimeException если пользователь не найден
     */
    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        // Используем orElseThrow для генерации исключения, если пользователь не найден
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Пользователь с ID " + id + " не найден"));
    }

    /**
     * Получить пользователя по имени пользователя.
     *
     * @param username имя пользователя
     * @return Optional с пользователем
     */
    @Transactional(readOnly = true)
    public Optional<User> getUserByUsername(String username) {
        // Возвращаем Optional, чтобы вызывающий код мог обработать отсутствие пользователя
        return userRepository.findByUsername(username);
    }

    /**
     * Получить пользователей по роли.
     *
     * @param role роль пользователя
     * @return список пользователей с указанной ролью
     */
    @Transactional(readOnly = true)
    public List<User> getUsersByRole(UserRole role) {
        return userRepository.findByRole(role);
    }

    /**
     * Получить всех активных юристов.
     * Используется для назначения юристов на дела и консультации.
     *
     * @return список активных юристов
     */
    @Transactional(readOnly = true)
    public List<User> getActiveLawyers() {
        // Получаем юристов с флагом enabled = true
        return userRepository.findByRoleAndEnabled(UserRole.LAWYER, true);
    }

    /**
     * Создать нового пользователя.
     *
     * @param user новый пользователь
     * @return сохраненный пользователь
     * @throws RuntimeException если пользователь с таким именем или email уже существует
     */
    public User createUser(User user) {
        // Проверяем, не существует ли пользователь с таким именем
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Пользователь с именем " + user.getUsername() + " уже существует");
        }

        // Проверяем, не существует ли пользователь с таким email
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Пользователь с email " + user.getEmail() + " уже существует");
        }

        // Хешируем пароль перед сохранением для безопасности
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Сохраняем пользователя в базе данных
        return userRepository.save(user);
    }

    /**
     * Обновить существующего пользователя.
     *
     * @param id идентификатор пользователя
     * @param updatedUser обновленные данные пользователя
     * @return обновленный пользователь
     * @throws RuntimeException если пользователь не найден
     */
    public User updateUser(Long id, User updatedUser) {
        // Получаем существующего пользователя
        User existingUser = getUserById(id);

        // Проверяем, не занято ли новое имя пользователя другим пользователем
        if (!existingUser.getUsername().equals(updatedUser.getUsername()) &&
                userRepository.existsByUsername(updatedUser.getUsername())) {
            throw new RuntimeException("Пользователь с именем " + updatedUser.getUsername() + " уже существует");
        }

        // Проверяем, не занят ли новый email другим пользователем
        if (!existingUser.getEmail().equals(updatedUser.getEmail()) &&
                userRepository.existsByEmail(updatedUser.getEmail())) {
            throw new RuntimeException("Пользователь с email " + updatedUser.getEmail() + " уже существует");
        }

        // Обновляем поля существующего пользователя
        existingUser.setUsername(updatedUser.getUsername());
        existingUser.setEmail(updatedUser.getEmail());
        existingUser.setFullName(updatedUser.getFullName());
        existingUser.setPhoneNumber(updatedUser.getPhoneNumber());
        existingUser.setRole(updatedUser.getRole());
        existingUser.setEnabled(updatedUser.getEnabled());

        // Если пароль был изменен (не пустой), хешируем и обновляем его
        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        }

        // Сохраняем обновленного пользователя
        return userRepository.save(existingUser);
    }

    /**
     * Удалить пользователя.
     *
     * @param id идентификатор пользователя
     * @throws RuntimeException если пользователь не найден
     */
    public void deleteUser(Long id) {
        // Проверяем существование пользователя
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("Пользователь с ID " + id + " не найден");
        }

        // Удаляем пользователя из базы данных
        userRepository.deleteById(id);
    }

    /**
     * Изменить роль пользователя.
     * Используется администратором для управления доступом.
     *
     * @param id идентификатор пользователя
     * @param newRole новая роль
     * @return обновленный пользователь
     */
    public User changeUserRole(Long id, UserRole newRole) {
        User user = getUserById(id);

        // Устанавливаем новую роль
        user.setRole(newRole);

        // Сохраняем изменения
        return userRepository.save(user);
    }

    /**
     * Активировать или деактивировать пользователя.
     *
     * @param id идентификатор пользователя
     * @param enabled флаг активности
     * @return обновленный пользователь
     */
    public User toggleUserStatus(Long id, boolean enabled) {
        // Получаем пользователя
        User user = getUserById(id);

        // Устанавливаем статус активности
        user.setEnabled(enabled);

        // Сохраняем изменения
        return userRepository.save(user);
    }

    /**
     * Получить количество пользователей.
     *
     * @return общее количество пользователей
     */
    @Transactional(readOnly = true)
    public long getUserCount() {
        // Используем метод count() репозитория
        return userRepository.count();
    }

    /**
     * Получить количество пользователей по роли.
     *
     * @param role роль пользователя
     * @return количество пользователей с указанной ролью
     */
    @Transactional(readOnly = true)
    public long getUserCountByRole(UserRole role) {
        // Получаем список пользователей и возвращаем его размер
        return userRepository.findByRole(role).size();
    }

    /**
     * Проверить, существует ли пользователь с заданным именем.
     *
     * @param username имя пользователя
     * @return true, если пользователь существует
     */
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * Проверить, существует ли пользователь с заданным email.
     *
     * @param email email пользователя
     * @return true, если пользователь существует
     */
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}