package ru.fa.legal.config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.fa.legal.model.*;
import ru.fa.legal.repository.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Класс для инициализации начальных данных в базе данных.
 * Создает тестовых пользователей, дела, консультации и документы.
 *
 * @author Киселева Ольга Ивановна
 * @version 1.0
 */
@Configuration
public class DataInitializer {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CaseRepository caseRepository;

    @Autowired
    private ConsultationRepository consultationRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Bean для загрузки начальных данных при старте приложения.
     *
     * @return CommandLineRunner
     */
    @Bean
    public CommandLineRunner loadInitialData() {
        return args -> {
            if (userRepository.count() > 10) {
                System.out.println("=================================================");
                System.out.println("База данных уже содержит данные. Пропуск инициализации.");
                System.out.println("=================================================");
                return;
            }

            System.out.println("=================================================");
            System.out.println("Начало инициализации начальных данных...");
            System.out.println("=================================================");

            User admin = User.builder()
                    .username("admin12")
                    .password(passwordEncoder.encode("admin123"))
                    .email("admin321@legalfirm.ru")
                    .fullName("Администратор Системы")
                    .phoneNumber("+7 (495) 123-45-67")
                    .role(UserRole.ADMIN)
                    .enabled(true)
                    .build();
            userRepository.save(admin);
            System.out.println("✓ Создан администратор: admin / admin123");

            // Менеджер
            User manager = User.builder()
                    .username("manager12")
                    .password(passwordEncoder.encode("manager123"))
                    .email("manager321@legalfirm.ru")
                    .fullName("Петров Петр Петрович")
                    .phoneNumber("+7 (495) 123-45-68")
                    .role(UserRole.MANAGER)
                    .enabled(true)
                    .build();
            userRepository.save(manager);
            System.out.println("✓ Создан менеджер: manager / manager123");

            // Юристы
            User lawyer1 = User.builder()
                    .username("lawyer12")
                    .password(passwordEncoder.encode("lawyer123"))
                    .email("ivanov231@legalfirm.ru")
                    .fullName("Иванов Иван Иванович")
                    .phoneNumber("+7 (495) 123-45-69")
                    .role(UserRole.LAWYER)
                    .enabled(true)
                    .build();
            userRepository.save(lawyer1);
            System.out.println("✓ Создан юрист: lawyer1 / lawyer123");

            User lawyer2 = User.builder()
                    .username("lawyer22")
                    .password(passwordEncoder.encode("lawyer123"))
                    .email("sidorova321@legalfirm.ru")
                    .fullName("Сидорова Анна Сергеевна")
                    .phoneNumber("+7 (495) 123-45-70")
                    .role(UserRole.LAWYER)
                    .enabled(true)
                    .build();
            userRepository.save(lawyer2);
            System.out.println("✓ Создан юрист: lawyer2 / lawyer123");

            User client1 = User.builder()
                    .username("client12")
                    .password(passwordEncoder.encode("client123"))
                    .email("smirnov23@example.com")
                    .fullName("Смирнов Алексей Владимирович")
                    .phoneNumber("+7 (916) 123-45-67")
                    .role(UserRole.CLIENT)
                    .enabled(true)
                    .build();
            userRepository.save(client1);
            System.out.println("✓ Создан клиент: client1 / client123");

            User client2 = User.builder()
                    .username("client22")
                    .password(passwordEncoder.encode("client123"))
                    .email("kuznetsova@example.com")
                    .fullName("Кузнецова Елена Дмитриевна")
                    .phoneNumber("+7 (916) 234-56-78")
                    .role(UserRole.CLIENT)
                    .enabled(true)
                    .build();
            userRepository.save(client2);
            System.out.println("✓ Создан клиент: client2 / client123");

            User client3 = User.builder()
                    .username("client32")
                    .password(passwordEncoder.encode("client123"))
                    .email("popov@example.com")
                    .fullName("Попов Дмитрий Николаевич")
                    .phoneNumber("+7 (916) 345-67-89")
                    .role(UserRole.CLIENT)
                    .enabled(true)
                    .build();
            userRepository.save(client3);
            System.out.println("✓ Создан клиент: client3 / client123");

            // ===== СОЗДАНИЕ ДЕЛ =====

            // Дело 1
            Case case1 = Case.builder()
                    .caseNumber("2025/011/ГР")
                    .title("Иск о взыскании задолженности по договору займа")
                    .description("Взыскание задолженности по договору займа от 15.01.2024 в размере 500 000 рублей")
                    .category(CaseCategory.CIVIL)
                    .status(CaseStatus.IN_PROGRESS)
                    .client(client1)
                    .lawyer(lawyer1)
                    .openDate(LocalDate.of(2025, 1, 15))
                    .cost(new BigDecimal("50000.00"))
                    .priority(Priority.HIGH)
                    .nextHearingDate(LocalDate.of(2025, 2, 20))
                    .notes("Требуется подготовить дополнительные доказательства")
                    .build();
            caseRepository.save(case1);
            System.out.println("✓ Создано дело: " + case1.getCaseNumber());

            // Дело 2
            Case case2 = Case.builder()
                    .caseNumber("2025/012/СЕМ")
                    .title("Расторжение брака и раздел имущества")
                    .description("Расторжение брака между супругами и раздел совместно нажитого имущества")
                    .category(CaseCategory.FAMILY)
                    .status(CaseStatus.AWAITING_HEARING)
                    .client(client2)
                    .lawyer(lawyer2)
                    .openDate(LocalDate.of(2025, 1, 20))
                    .cost(new BigDecimal("75000.00"))
                    .priority(Priority.MEDIUM)
                    .nextHearingDate(LocalDate.of(2025, 3, 5))
                    .notes("Подготовлена опись имущества")
                    .build();
            caseRepository.save(case2);
            System.out.println("✓ Создано дело: " + case2.getCaseNumber());

            // Дело 3
            Case case3 = Case.builder()
                    .caseNumber("2025/013/ТРУ")
                    .title("Восстановление на работе")
                    .description("Оспаривание незаконного увольнения и восстановление в должности")
                    .category(CaseCategory.LABOR)
                    .status(CaseStatus.NEW)
                    .client(client3)
                    .lawyer(lawyer1)
                    .openDate(LocalDate.of(2025, 1, 25))
                    .cost(new BigDecimal("40000.00"))
                    .priority(Priority.HIGH)
                    .notes("Собираются документы от работодателя")
                    .build();
            caseRepository.save(case3);
            System.out.println("✓ Создано дело: " + case3.getCaseNumber());

            // Дело 4
            Case case4 = Case.builder()
                    .caseNumber("2024/187/ГР")
                    .title("Взыскание убытков по договору поставки")
                    .description("Взыскание убытков за нарушение условий договора поставки товара")
                    .category(CaseCategory.CIVIL)
                    .status(CaseStatus.WON)
                    .client(client1)
                    .lawyer(lawyer2)
                    .openDate(LocalDate.of(2024, 10, 5))
                    .closeDate(LocalDate.of(2025, 1, 10))
                    .cost(new BigDecimal("60000.00"))
                    .priority(Priority.MEDIUM)
                    .notes("Дело выиграно, решение суда исполнено")
                    .build();
            caseRepository.save(case4);
            System.out.println("✓ Создано дело: " + case4.getCaseNumber());

            // ===== СОЗДАНИЕ ДОКУМЕНТОВ =====

            // Документ 1
            Document doc1 = Document.builder()
                    .caseEntity(case1)
                    .title("Исковое заявление о взыскании задолженности")
                    .documentType(DocumentType.COMPLAINT)
                    .description("Исковое заявление в Измайловский районный суд г. Москвы")
                    .documentNumber("ИСК-011/2025")
                    .documentDate(LocalDate.of(2025, 1, 15))
                    .status(DocumentStatus.SENT)
                    .isImportant(true)
                    .notes("Отправлено в суд 16.01.2025")
                    .build();
            documentRepository.save(doc1);
            System.out.println("✓ Создан документ: " + doc1.getTitle());

            // Документ 2
            Document doc2 = Document.builder()
                    .caseEntity(case1)
                    .title("Договор об оказании юридических услуг")
                    .documentType(DocumentType.CONTRACT)
                    .description("Договор с клиентом на ведение дела")
                    .documentNumber("Д-2025/101")
                    .documentDate(LocalDate.of(2025, 1, 15))
                    .status(DocumentStatus.SIGNED)
                    .isImportant(true)
                    .notes("Подписан обеими сторонами")
                    .build();
            documentRepository.save(doc2);
            System.out.println("✓ Создан документ: " + doc2.getTitle());

            // Документ 3
            Document doc3 = Document.builder()
                    .caseEntity(case2)
                    .title("Заявление о расторжении брака")
                    .documentType(DocumentType.APPLICATION)
                    .description("Заявление о расторжении брака и разделе имущества")
                    .documentNumber("ЗАВ-102/2025")
                    .documentDate(LocalDate.of(2025, 1, 20))
                    .status(DocumentStatus.APPROVED)
                    .isImportant(true)
                    .notes("Принято судом к рассмотрению")
                    .build();
            documentRepository.save(doc3);
            System.out.println("✓ Создан документ: " + doc3.getTitle());

            // ===== СОЗДАНИЕ КОНСУЛЬТАЦИЙ =====

            // Консультация 1
            Consultation consultation1 = Consultation.builder()
                    .client(client1)
                    .lawyer(lawyer1)
                    .consultationDate(LocalDateTime.of(2025, 2, 10, 14, 0))
                    .durationMinutes(60)
                    .topic("Обсуждение хода дела о взыскании задолженности")
                    .description("Консультация по текущему состоянию дела и дальнейшей стратегии")
                    .type(ConsultationType.OFFICE)
                    .status(ConsultationStatus.SCHEDULED)
                    .cost(new BigDecimal("3000.00"))
                    .isPaid(false)
                    .reminderSent(false)
                    .build();
            consultationRepository.save(consultation1);
            System.out.println("✓ Создана консультация: " + consultation1.getTopic());

            // Консультация 2
            Consultation consultation2 = Consultation.builder()
                    .client(client2)
                    .lawyer(lawyer2)
                    .consultationDate(LocalDateTime.of(2025, 2, 5, 15, 30))
                    .durationMinutes(90)
                    .topic("Консультация по семейному спору")
                    .description("Обсуждение раздела имущества и алиментов")
                    .type(ConsultationType.ONLINE)
                    .status(ConsultationStatus.CONFIRMED)
                    .cost(new BigDecimal("4000.00"))
                    .isPaid(true)
                    .reminderSent(true)
                    .build();
            consultationRepository.save(consultation2);
            System.out.println("✓ Создана консультация: " + consultation2.getTopic());

            // Консультация 3
            Consultation consultation3 = Consultation.builder()
                    .client(client3)
                    .lawyer(lawyer1)
                    .consultationDate(LocalDateTime.of(2025, 1, 28, 10, 0))
                    .durationMinutes(60)
                    .topic("Первичная консультация по трудовому спору")
                    .description("Анализ ситуации и определение правовой позиции")
                    .type(ConsultationType.OFFICE)
                    .status(ConsultationStatus.COMPLETED)
                    .cost(new BigDecimal("2500.00"))
                    .isPaid(true)
                    .reminderSent(true)
                    .lawyerNotes("Клиент предоставил все необходимые документы. Дело перспективное.")
                    .result("Принято решение о подаче иска в суд")
                    .build();
            consultationRepository.save(consultation3);
            System.out.println("✓ Создана консультация: " + consultation3.getTopic());

            System.out.println("=================================================");
            System.out.println("Инициализация данных завершена успешно!");
            System.out.println("=================================================");
            System.out.println("\nДоступные учетные записи:");
            System.out.println("  Администратор: admin12 / admin123");
            System.out.println("  Менеджер:      manager12 / manager123");
            System.out.println("  Юрист 1:       lawyer12 / lawyer123");
            System.out.println("  Юрист 2:       lawyer22 / lawyer123");
            System.out.println("  Клиент 1:      client11 / client123");
            System.out.println("  Клиент 2:      client22 / client123");
            System.out.println("  Клиент 3:      client32 / client123");
            System.out.println("=================================================");
        };
    }
}