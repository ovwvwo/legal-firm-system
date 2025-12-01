package ru.fa.legal;

// Импорт Spring Boot аннотаций
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Главный класс приложения информационно-справочной системы юридической фирмы.
 * Точка входа в Spring Boot приложение.
 *
 * @SpringBootApplication - комбинированная аннотация, включающая:
 *   - @Configuration: помечает класс как источник конфигурации Spring
 *   - @EnableAutoConfiguration: включает автоматическую конфигурацию Spring Boot
 *   - @ComponentScan: сканирует пакет на наличие Spring компонентов
 *
 * @author Kiseleva Olga
 * @version 1.0
 * @since 2025-10-01
 */
@SpringBootApplication
public class LegalFirmApplication {

    /**
     * Главный метод приложения.
     * Запускает Spring Boot приложение.
     *
     * @param args аргументы командной строки
     */
    public static void main(String[] args) {
        // SpringApplication.run() инициализирует Spring контекст
        // и запускает встроенный Tomcat сервер
        SpringApplication.run(LegalFirmApplication.class, args);
        System.out.println("=================================================");
        System.out.println("Информационно-справочная система юридической фирмы");
        System.out.println("Приложение успешно запущено!");
        System.out.println("Доступно по адресу: http://localhost:8080");
        System.out.println("=================================================");
    }

    /**
     * Bean для кодирования паролей.
     * BCryptPasswordEncoder использует алгоритм bcrypt для безопасного хеширования паролей.
     *
     * @Bean - помечает метод как создатель Spring bean
     * @return экземпляр PasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        // Создаем и возвращаем BCryptPasswordEncoder
        // Он автоматически генерирует соль и хеширует пароли
        return new BCryptPasswordEncoder();
    }
}