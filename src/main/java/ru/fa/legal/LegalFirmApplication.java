package ru.fa.legal;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
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
        SpringApplication.run(LegalFirmApplication.class, args);
        System.out.println("=================================================");
        System.out.println("Информационно-справочная система юридической фирмы");
        System.out.println("Приложение успешно запущено!");
        System.out.println("Доступно по адресу: http://localhost:9090");
        System.out.println("=================================================");
    }

    /**
     * Bean для кодирования паролей.
     * BCryptPasswordEncoder использует алгоритм bcrypt для безопасного хеширования паролей.
     *
     * @Bean - помечает метод как создатель Spring bean
     * @return экземпляр PasswordEncoder
     */
    // @Bean
    // public PasswordEncoder passwordEncoder() {
        // Создаем и возвращаем BCryptPasswordEncoder
        // Он автоматически генерирует соль и хеширует пароли
       //  return new BCryptPasswordEncoder();
   // }
}