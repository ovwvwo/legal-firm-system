package ru.fa.legal.service;

// Импорт Spring Security классов
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Импорт моделей и репозиториев
import ru.fa.legal.model.User;
import ru.fa.legal.repository.UserRepository;

// Импорт классов для работы с коллекциями
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Реализация UserDetailsService для Spring Security.
 * Загружает данные пользователя для аутентификации.
 *
 * UserDetailsService - интерфейс Spring Security для загрузки пользовательских данных.
 *
 * @author Иванов Егор Борисович
 * @version 1.0
 */
// @Service - помечает класс как сервисный компонент Spring
@Service
// @Transactional - все методы выполняются в транзакциях
@Transactional
public class UserDetailsServiceImpl implements UserDetailsService {

    /**
     * Репозиторий для доступа к данным пользователей.
     */
    @Autowired
    private UserRepository userRepository;

    /**
     * Загружает пользователя по имени пользователя.
     * Вызывается Spring Security при попытке входа пользователя.
     *
     * @param username имя пользователя
     * @return объект UserDetails с данными пользователя
     * @throws UsernameNotFoundException если пользователь не найден
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Ищем пользователя в базе данных по имени пользователя
        User user = userRepository.findByUsername(username)
                // Если пользователь не найден, выбрасываем исключение
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Пользователь с именем " + username + " не найден"));

        // Создаем и возвращаем объект UserDetails для Spring Security
        // org.springframework.security.core.userdetails.User - встроенный класс Spring Security
        return new org.springframework.security.core.userdetails.User(
                // Имя пользователя
                user.getUsername(),
                // Зашифрованный пароль
                user.getPassword(),
                // Статус активности учетной записи
                user.getEnabled(),
                // accountNonExpired - учетная запись не истекла (true = не истекла)
                true,
                // credentialsNonExpired - учетные данные не истекли
                true,
                // accountNonLocked - учетная запись не заблокирована
                true,
                // Список ролей и прав доступа пользователя
                getAuthorities(user)
        );
    }

    /**
     * Получает список полномочий (authorities) пользователя на основе его роли.
     * Полномочия используются Spring Security для контроля доступа.
     *
     * @param user пользователь
     * @return коллекция полномочий
     */
    private Collection<? extends GrantedAuthority> getAuthorities(User user) {
        // Создаем список для хранения полномочий
        List<GrantedAuthority> authorities = new ArrayList<>();

        // Добавляем роль пользователя как полномочие
        // Spring Security требует префикс "ROLE_" перед названием роли
        // Например, роль ADMIN становится "ROLE_ADMIN"
        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));

        // Можно добавить дополнительные полномочия в зависимости от роли
        // Например, специфические права доступа
        switch (user.getRole()) {
            case ADMIN:
                // Администратор получает все права
                authorities.add(new SimpleGrantedAuthority("MANAGE_USERS"));
                authorities.add(new SimpleGrantedAuthority("MANAGE_CASES"));
                authorities.add(new SimpleGrantedAuthority("VIEW_STATISTICS"));
                authorities.add(new SimpleGrantedAuthority("MANAGE_DOCUMENTS"));
                break;

            case MANAGER:
                // Менеджер получает права на управление делами и просмотр статистики
                authorities.add(new SimpleGrantedAuthority("MANAGE_CASES"));
                authorities.add(new SimpleGrantedAuthority("VIEW_STATISTICS"));
                authorities.add(new SimpleGrantedAuthority("MANAGE_DOCUMENTS"));
                break;

            case LAWYER:
                // Юрист получает права на работу с делами и документами
                authorities.add(new SimpleGrantedAuthority("MANAGE_CASES"));
                authorities.add(new SimpleGrantedAuthority("MANAGE_DOCUMENTS"));
                authorities.add(new SimpleGrantedAuthority("CONDUCT_CONSULTATIONS"));
                break;

            case CLIENT:
                // Клиент получает только право просмотра своих данных
                authorities.add(new SimpleGrantedAuthority("VIEW_OWN_CASES"));
                authorities.add(new SimpleGrantedAuthority("BOOK_CONSULTATION"));
                break;

            default:
                // На случай добавления новых ролей
                break;
        }

        // Возвращаем список полномочий
        return authorities;
    }

    /**
     * Дополнительный метод для загрузки пользователя по email.
     * Может использоваться для альтернативного способа входа.
     *
     * @param email email пользователя
     * @return объект UserDetails с данными пользователя
     * @throws UsernameNotFoundException если пользователь не найден
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserByEmail(String email) throws UsernameNotFoundException {
        // Ищем пользователя в базе данных по email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Пользователь с email " + email + " не найден"));

        // Создаем и возвращаем объект UserDetails
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.getEnabled(),
                true,
                true,
                true,
                getAuthorities(user)
        );
    }
}