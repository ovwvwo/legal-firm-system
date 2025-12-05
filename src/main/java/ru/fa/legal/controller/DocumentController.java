package ru.fa.legal.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.fa.legal.model.*;
import ru.fa.legal.service.CaseService;
import ru.fa.legal.service.DocumentService;
import ru.fa.legal.service.UserService;
import jakarta.validation.Valid;
import java.util.List;

@Controller
@RequestMapping("/documents")
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    @Autowired
    private CaseService caseService;

    @Autowired
    private UserService userService;

    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('LAWYER', 'MANAGER', 'ADMIN')")
    public String listDocuments(Model model, Authentication authentication) {
        String username = authentication.getName();

        User currentUser = userService.getUserByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        List<Document> documents;

        switch (currentUser.getRole()) {
            case LAWYER:
                List<Case> lawyerCases = caseService.getCasesByLawyer(currentUser.getId());
                documents = documentService.getAllDocuments().stream()
                        .filter(d -> lawyerCases.contains(d.getCaseEntity()))
                        .toList();
                break;

            case MANAGER:
            case ADMIN:
                documents = documentService.getAllDocuments();
                break;

            default:
                documents = List.of();
                break;
        }

        model.addAttribute("documents", documents);
        model.addAttribute("currentUser", currentUser);

        return "documents/list";
    }

    @GetMapping("/case/{caseId}")
    public String listDocumentsByCase(@PathVariable Long caseId, Model model) {
        Case caseEntity = caseService.getCaseById(caseId);
        List<Document> documents = documentService.getDocumentsByCase(caseId);
        model.addAttribute("caseEntity", caseEntity);
        model.addAttribute("documents", documents);
        return "documents/list";
    }

    @GetMapping("/add")
    @PreAuthorize("hasAnyRole('LAWYER', 'MANAGER', 'ADMIN')")
    public String showAddForm(@RequestParam(required = false) Long caseId, Model model) {
        Document document = new Document();
        if (caseId != null) {
            Case caseEntity = caseService.getCaseById(caseId);
            document.setCaseEntity(caseEntity);
        }
        model.addAttribute("document", document);

        model.addAttribute("documentTypes", DocumentType.values());
        model.addAttribute("documentStatuses", DocumentStatus.values());

        model.addAttribute("cases", caseService.getAllCases());
        return "documents/add";
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAnyRole('LAWYER', 'MANAGER', 'ADMIN')")
    public String showEditForm(@PathVariable Long id, Model model) {
        Document document = documentService.getDocumentById(id);
        model.addAttribute("document", document);
        model.addAttribute("documentTypes", DocumentType.values());
        model.addAttribute("documentStatuses", DocumentStatus.values());
        model.addAttribute("cases", caseService.getAllCases());
        return "documents/edit";
    }

    @PostMapping("/save")
    @PreAuthorize("hasAnyRole('LAWYER', 'MANAGER', 'ADMIN')")
    public String saveDocument(@Valid @ModelAttribute("document") Document document,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes,
                               Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("documentTypes", DocumentType.values());
            model.addAttribute("documentStatuses", DocumentStatus.values());
            model.addAttribute("cases", caseService.getAllCases());

            if (document.getId() == null) {
                return "documents/add";
            } else {
                return "documents/edit";
            }
        }

        try {
            // ВАЖНО: Загружаем полный объект дела из БД
            if (document.getCaseEntity() != null && document.getCaseEntity().getId() != null) {
                Case caseEntity = caseService.getCaseById(document.getCaseEntity().getId());
                document.setCaseEntity(caseEntity);
            }

            if (document.getId() == null) {
                Document savedDocument = documentService.createDocument(document);
                redirectAttributes.addFlashAttribute("successMessage",
                        "Документ '" + savedDocument.getTitle() + "' успешно создан");

                return "redirect:/documents/case/" + savedDocument.getCaseEntity().getId();
            } else {
                Document updatedDocument = documentService.updateDocument(document.getId(), document);
                redirectAttributes.addFlashAttribute("successMessage",
                        "Документ '" + updatedDocument.getTitle() + "' успешно обновлен");

                return "redirect:/documents/view/" + document.getId();
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при сохранении документа: " + e.getMessage());

            if (document.getId() == null) {
                return "redirect:/documents/add";
            } else {
                return "redirect:/documents/edit/" + document.getId();
            }
        }
    }

    @PostMapping("/delete/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public String deleteDocument(@PathVariable Long id,
                                 RedirectAttributes redirectAttributes) {
        try {
            Document document = documentService.getDocumentById(id);
            Long caseId = document.getCaseEntity().getId();
            String title = document.getTitle();

            documentService.deleteDocument(id);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Документ '" + title + "' успешно удален");

            return "redirect:/documents/case/" + caseId;

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при удалении документа: " + e.getMessage());
            return "redirect:/documents/list";
        }
    }

    @GetMapping("/view/{id}")
    public String viewDocument(@PathVariable Long id, Model model) {
        Document document = documentService.getDocumentById(id);
        model.addAttribute("document", document);
        return "documents/view";
    }

    @PostMapping("/change-status/{id}")
    @PreAuthorize("hasAnyRole('LAWYER', 'MANAGER', 'ADMIN')")
    public String changeDocumentStatus(@PathVariable Long id,
                                       @RequestParam DocumentStatus status,
                                       RedirectAttributes redirectAttributes) {
        try {
            Document document = documentService.changeDocumentStatus(id, status);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Статус документа изменен на '" + status.getDisplayName() + "'");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при изменении статуса: " + e.getMessage());
        }
        return "redirect:/documents/view/" + id;
    }

    @PostMapping("/toggle-important/{id}")
    @PreAuthorize("hasAnyRole('LAWYER', 'MANAGER', 'ADMIN')")
    public String toggleImportant(@PathVariable Long id,
                                  RedirectAttributes redirectAttributes) {
        try {
            Document document = documentService.getDocumentById(id);
            boolean newStatus = !document.getIsImportant();
            documentService.markAsImportant(id, newStatus);
            String message = newStatus ? "Документ помечен как важный" : "С документа снята пометка важности";
            redirectAttributes.addFlashAttribute("successMessage", message);

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при изменении важности: " + e.getMessage());
        }
        return "redirect:/documents/view/" + id;
    }

    @GetMapping("/filter")
    public String filterDocuments(@RequestParam Long caseId,
                                  @RequestParam(required = false) DocumentType type,
                                  Model model) {

        Case caseEntity = caseService.getCaseById(caseId);
        List<Document> documents;
        if (type == null) {
            documents = documentService.getDocumentsByCase(caseId);
        } else {
            documents = documentService.getDocumentsByCaseAndType(caseId, type);
        }
        model.addAttribute("caseEntity", caseEntity);
        model.addAttribute("documents", documents);
        model.addAttribute("selectedType", type);
        model.addAttribute("documentTypes", DocumentType.values());
        return "documents/list";
    }
}