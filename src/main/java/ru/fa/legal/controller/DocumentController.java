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
import ru.fa.legal.model.*;
import ru.fa.legal.service.DocumentService;
import ru.fa.legal.service.UserService;

// Импорт аннотаций валидации
import jakarta.validation.Valid;

// Импорт классов для работы с коллекциями
import java.util.List;

/**
 * Контроллер для управления документами.
 * Обрабатывает HTTP запросы, связанные с документами.
 * Реализует работу с дочерними сущностями в связи "родитель-дочка".
 *
 * @author Иванов Егор Борисович
 * @version 1.0
 */
@Controller
@RequestMapping("/documents")
public class DocumentController {

    /**
     * Сервис для работы с документами.
     */
    @Autowired
    private DocumentService documentService;

    /**
     * Сервис для работы с делами (родительские сущности).
     */
    @Autowired
    private CaseService caseService;

    /**
     * Сервис для работы с пользователями.
     */
    @Autowired
    private UserService userService;

    /**
     * Отображает список всех документов.
     * Доступно юристам, менеджерам и администраторам.
     *
     * @param model модель для передачи данных
     * @param authentication объект аутентификации текущего пользователя
     * @return имя шаблона со списком документов
     */
    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('LAWYER', 'MANAGER', 'ADMIN')")
    public String listDocuments(Model model, Authentication authentication) {
        // Получаем имя текущего пользователя
        String username = authentication.getName();

        // Получаем текущего пользователя
        User currentUser = userService.getUserByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        // Список документов зависит от роли
        List<Document> documents;

        // Определяем, какие документы показывать в зависимости от роли
        switch (currentUser.getRole()) {
            case LAWYER:
                // Юрист видит документы по своим делам
                List<Case> lawyerCases = caseService.getCasesByLawyer(currentUser.getId());
                documents = documentService.getAllDocuments().stream()
                        .filter(d -> lawyerCases.contains(d.getCaseEntity()))
                        .toList();
                break;

            case MANAGER:
            case ADMIN:
                // Менеджер и администратор видят все документы
                documents = documentService.getAllDocuments();
                break;

            default:
                // По умолчанию пустой список
                documents = List.of();
                break;
        }

        // Добавляем список документов в модель
        model.addAttribute("documents", documents);
        model.addAttribute("currentUser", currentUser);

        // Возвращаем шаблон documents/list.html
        return "documents/list";
    }

    /**
     * Отображает список документов для конкретного дела.
     * Реализует просмотр дочерних записей для родителя.
     *
     * @param caseId идентификатор дела (родительской сущности)
     * @param model модель для передачи данных
     * @return имя шаблона со списком документов
     */
    @GetMapping("/case/{caseId}")
    public String listDocumentsByCase(@PathVariable Long caseId, Model model) {
        // Получаем дело (родительскую сущность)
        Case caseEntity = caseService.getCaseById(caseId);

        // Получаем все документы дела (дочерние записи)
        List<Document> documents = documentService.getDocumentsByCase(caseId);

        // Добавляем данные в модель
        model.addAttribute("caseEntity", caseEntity);
        model.addAttribute("documents", documents);

        // Возвращаем шаблон
        return "documents/list";
    }

    /**
     * Отображает форму для добавления нового документа.
     *
     * @param caseId идентификатор дела (необязательный параметр)
     * @param model модель для передачи данных
     * @return имя шаблона формы добавления
     */
    @GetMapping("/add")
    @PreAuthorize("hasAnyRole('LAWYER', 'MANAGER', 'ADMIN')")
    public String showAddForm(@RequestParam(required = false) Long caseId, Model model) {
        // Создаем пустой объект документа
        Document document = new Document();

        // Если указан ID дела, устанавливаем связь с родителем
        if (caseId != null) {
            Case caseEntity = caseService.getCaseById(caseId);
            document.setCaseEntity(caseEntity);
        }

        // Добавляем объект в модель
        model.addAttribute("document", document);

        // Добавляем списки для выпадающих списков
        model.addAttribute("documentTypes", DocumentType.values());
        model.addAttribute("documentStatuses", DocumentStatus.values());

        // Получаем список всех дел для выбора родителя
        model.addAttribute("cases", caseService.getAllCases());

        // Возвращаем шаблон documents/add.html
        return "documents/add";
    }

    /**
     * Отображает форму редактирования документа.
     *
     * @param id идентификатор документа
     * @param model модель для передачи данных
     * @return имя шаблона формы редактирования
     */
    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAnyRole('LAWYER', 'MANAGER', 'ADMIN')")
    public String showEditForm(@PathVariable Long id, Model model) {
        // Получаем документ
        Document document = documentService.getDocumentById(id);

        // Добавляем документ в модель
        model.addAttribute("document", document);

        // Добавляем справочные данные
        model.addAttribute("documentTypes", DocumentType.values());
        model.addAttribute("documentStatuses", DocumentStatus.values());
        model.addAttribute("cases", caseService.getAllCases());

        // Возвращаем шаблон documents/edit.html
        return "documents/edit";
    }

    /**
     * Сохраняет документ (создание или обновление).
     * Реализует добавление/обновление дочерней записи.
     *
     * @param document данные документа из формы
     * @param bindingResult результаты валидации
     * @param redirectAttributes атрибуты для сообщений
     * @return URL для перенаправления
     */
    @PostMapping("/save")
    @PreAuthorize("hasAnyRole('LAWYER', 'MANAGER', 'ADMIN')")
    public String saveDocument(@Valid @ModelAttribute("document") Document document,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes) {

        // Проверяем ошибки валидации
        if (bindingResult.hasErrors()) {
            // Возвращаем к форме
            if (document.getId() == null) {
                return "documents/add";
            } else {
                return "documents/edit";
            }
        }

        try {
            // Определяем, создание или обновление
            if (document.getId() == null) {
                // Создание нового документа (добавление дочерней записи)
                Document savedDocument = documentService.createDocument(document);
                redirectAttributes.addFlashAttribute("successMessage",
                        "Документ '" + savedDocument.getTitle() + "' успешно создан");

                // Перенаправляем к списку документов дела
                return "redirect:/documents/case/" + savedDocument.getCaseEntity().getId();
            } else {
                // Обновление существующего документа
                Document updatedDocument = documentService.updateDocument(document.getId(), document);
                redirectAttributes.addFlashAttribute("successMessage",
                        "Документ '" + updatedDocument.getTitle() + "' успешно обновлен");

                return "redirect:/documents/view/" + document.getId();
            }

        } catch (Exception e) {
            // Обрабатываем ошибку
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при сохранении документа: " + e.getMessage());

            if (document.getId() == null) {
                return "redirect:/documents/add";
            } else {
                return "redirect:/documents/edit/" + document.getId();
            }
        }
    }

    /**
     * Удаляет документ.
     * Удаляет дочернюю запись, не затрагивая родителя.
     *
     * @param id идентификатор документа
     * @param redirectAttributes атрибуты для сообщений
     * @return URL для перенаправления
     */
    @PostMapping("/delete/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public String deleteDocument(@PathVariable Long id,
                                 RedirectAttributes redirectAttributes) {
        try {
            // Получаем документ для сохранения информации о деле
            Document document = documentService.getDocumentById(id);
            Long caseId = document.getCaseEntity().getId();
            String title = document.getTitle();

            // Удаляем документ (дочернюю запись)
            documentService.deleteDocument(id);

            // Добавляем сообщение об успехе
            redirectAttributes.addFlashAttribute("successMessage",
                    "Документ '" + title + "' успешно удален");

            // Перенаправляем к списку документов дела (родителя)
            return "redirect:/documents/case/" + caseId;

        } catch (Exception e) {
            // Обрабатываем ошибку
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при удалении документа: " + e.getMessage());
            return "redirect:/documents/list";
        }
    }

    /**
     * Отображает подробную информацию о документе.
     *
     * @param id идентификатор документа
     * @param model модель для передачи данных
     * @return имя шаблона для отображения
     */
    @GetMapping("/view/{id}")
    public String viewDocument(@PathVariable Long id, Model model) {
        // Получаем документ
        Document document = documentService.getDocumentById(id);

        // Добавляем документ в модель
        model.addAttribute("document", document);

        // Возвращаем шаблон documents/view.html
        return "documents/view";
    }

    /**
     * Изменяет статус документа.
     *
     * @param id идентификатор документа
     * @param status новый статус
     * @param redirectAttributes атрибуты для сообщений
     * @return URL для перенаправления
     */
    @PostMapping("/change-status/{id}")
    @PreAuthorize("hasAnyRole('LAWYER', 'MANAGER', 'ADMIN')")
    public String changeDocumentStatus(@PathVariable Long id,
                                       @RequestParam DocumentStatus status,
                                       RedirectAttributes redirectAttributes) {
        try {
            // Изменяем статус
            Document document = documentService.changeDocumentStatus(id, status);

            // Добавляем сообщение об успехе
            redirectAttributes.addFlashAttribute("successMessage",
                    "Статус документа изменен на '" + status.getDisplayName() + "'");

        } catch (Exception e) {
            // Обрабатываем ошибку
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при изменении статуса: " + e.getMessage());
        }

        // Перенаправляем обратно
        return "redirect:/documents/view/" + id;
    }

    /**
     * Переключает флаг важности документа.
     *
     * @param id идентификатор документа
     * @param redirectAttributes атрибуты для сообщений
     * @return URL для перенаправления
     */
    @PostMapping("/toggle-important/{id}")
    @PreAuthorize("hasAnyRole('LAWYER', 'MANAGER', 'ADMIN')")
    public String toggleImportant(@PathVariable Long id,
                                  RedirectAttributes redirectAttributes) {
        try {
            // Получаем документ
            Document document = documentService.getDocumentById(id);

            // Переключаем флаг важности
            boolean newStatus = !document.getIsImportant();
            documentService.markAsImportant(id, newStatus);

            // Добавляем сообщение об успехе
            String message = newStatus ? "Документ помечен как важный" : "С документа снята пометка важности";
            redirectAttributes.addFlashAttribute("successMessage", message);

        } catch (Exception e) {
            // Обрабатываем ошибку
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при изменении важности: " + e.getMessage());
        }

        // Перенаправляем обратно
        return "redirect:/documents/view/" + id;
    }

    /**
     * Фильтрация документов дела по типу.
     *
     * @param caseId идентификатор дела
     * @param type тип документа
     * @param model модель для передачи данных
     * @return имя шаблона со списком документов
     */
    @GetMapping("/filter")
    public String filterDocuments(@RequestParam Long caseId,
                                  @RequestParam(required = false) DocumentType type,
                                  Model model) {

        // Получаем дело
        Case caseEntity = caseService.getCaseById(caseId);

        // Список документов
        List<Document> documents;

        // Если тип не указан, показываем все документы дела
        if (type == null) {
            documents = documentService.getDocumentsByCase(caseId);
        } else {
            // Иначе фильтруем по типу
            documents = documentService.getDocumentsByCaseAndType(caseId, type);
        }

        // Добавляем результаты в модель
        model.addAttribute("caseEntity", caseEntity);
        model.addAttribute("documents", documents);
        model.addAttribute("selectedType", type);
        model.addAttribute("documentTypes", DocumentType.values());

        // Возвращаем шаблон списка
        return "documents/list";
    }
}