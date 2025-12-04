package ru.fa.legal.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Конфигурация безопасности Spring Security.
 * Определяет правила доступа к ресурсам приложения.
 *
 * @Configuration - помечает класс как источник конфигурации Spring
 * @EnableWebSecurity - включает поддержку Spring Security
 * @EnableMethodSecurity - включает аннотации безопасности на уровне методов
 *
 * @author Киселева Ольга Ивановна
 * @version 1.0
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    /**
     * Настройка цепочки фильтров безопасности.
     * Определяет, какие URL требуют аутентификации и какие роли имеют доступ.
     *
     * @param http объект HttpSecurity для настройки безопасности
     * @return настроенная цепочка фильтров безопасности
     * @throws Exception если произошла ошибка конфигурации
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http.authorizeHttpRequests(authorize -> authorize
                        // Публичные URL (доступны без аутентификации)
                        // permitAll() - разрешает доступ всем пользователям
                        .requestMatchers("/", "/login", "/register", "/css/**", "/js/**", "/images/**").permitAll()

                        // URL для администраторов
                        // hasRole() - проверяет наличие указанной роли
                        .requestMatchers("/admin/**", "/users/**").hasRole("ADMIN")

                        // URL для менеджеров и администраторов
                        // hasAnyRole() - проверяет наличие любой из указанных ролей
                        .requestMatchers("/statistics/**").hasAnyRole("MANAGER", "ADMIN")

                        // URL для юристов, менеджеров и администраторов
                        .requestMatchers("/cases/add", "/cases/edit/**", "/documents/**").hasAnyRole("LAWYER", "MANAGER", "ADMIN")

                        // Все остальные запросы требуют аутентификации
                        // authenticated() - пользователь должен быть аутентифицирован
                        .anyRequest().authenticated()
                )

                // Настройка формы входа
                .formLogin(form -> form
                        .loginPage("/login")
                        // URL для обработки данных формы входа
                        .loginProcessingUrl("/perform_login")
                        // URL перенаправления после успешного входа
                        .defaultSuccessUrl("/dashboard", true)
                        // URL перенаправления при ошибке входа
                        .failureUrl("/login?error=true")
                        // Имя параметра для имени пользователя
                        .usernameParameter("username")
                        // Имя параметра для пароля
                        .passwordParameter("password")
                        // Разрешаем доступ к странице входа всем
                        .permitAll()
                )

                // Настройка выхода из системы
                .logout(logout -> logout
                        // URL для выхода
                        .logoutUrl("/logout")
                        // URL перенаправления после выхода
                        .logoutSuccessUrl("/login?logout=true")
                        // Инвалидация HTTP сессии при выходе
                        .invalidateHttpSession(true)
                        // Удаление cookies при выходе
                        .deleteCookies("JSESSIONID")
                        // Разрешаем доступ к функции выхода всем аутентифицированным пользователям
                        .permitAll()
                )

                // Настройка запоминания пользователя (Remember Me)
                .rememberMe(remember -> remember
                        // Ключ для шифрования cookie
                        .key("uniqueAndSecretKey")
                        // Время жизни cookie (в секундах) - 7 дней
                        .tokenValiditySeconds(604800)
                        // Имя параметра в форме входа
                        .rememberMeParameter("remember-me")
                )

                // Обработка исключений безопасности
                .exceptionHandling(exception -> exception
                        // Страница для не аутентифицированных пользователей
                        .authenticationEntryPoint((request, response, authException) ->
                                response.sendRedirect("/login"))
                        // Страница для пользователей без прав доступа
                        .accessDeniedPage("/access-denied")
                )

                // Отключаем CSRF защиту для REST API
                // В продакшене лучше оставить включенной!!!!!!
                .csrf(csrf -> csrf
                        // Игнорируем CSRF для API endpoints
                        .ignoringRequestMatchers("/api/**")
                );

        // Возвращаем построенную цепочку фильтров
        return http.build();
    }

    /**
     * Bean для AuthenticationManager.
     * Используется для программной аутентификации пользователей.
     *
     * @param authConfig конфигурация аутентификации
     * @return менеджер аутентификации
     * @throws Exception если произошла ошибка получения менеджера
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * Bean для кодирования паролей.
     * BCrypt - это криптографический алгоритм хеширования паролей.
     *
     * @return кодировщик паролей
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        // Создаем BCryptPasswordEncoder
        // По умолчанию использует силу 10 (количество раундов хеширования)
        return new BCryptPasswordEncoder();
    }
}