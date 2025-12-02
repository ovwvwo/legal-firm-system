package ru.fa.legal.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.fa.legal.model.Document;
import ru.fa.legal.model.Case;
import ru.fa.legal.model.DocumentType;
import ru.fa.legal.model.DocumentStatus;
import ru.fa.legal.repository.DocumentRepository;
import ru.fa.legal.repository.CaseRepository;

import java.util.List;

/**
 * Сервис для работы с документами.
 * Содержит бизнес-логику управления документами.
 * Реализует работу с дочерними сущностями в связи "родитель-дочка".
 *
 * @author Иванов Егор Борисович
 * @version 1.0
 */
@Service
@Transactional
public class DocumentService {

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private CaseRepository caseRepository;

    /**
     * Получить все документы.
     *
     * @return список всех документов
     */
    @Transactional(readOnly = true)
    public List<Document> getAllDocuments() {
        return documentRepository.findAll();
    }

    /**
     * Получить документ по ID.
     *
     * @param id идентификатор документа
     * @return документ
     * @throws RuntimeException если документ не найден
     */
    @Transactional(readOnly = true)
    public Document getDocumentById(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Документ с ID " + id + " не найден"));
    }

    /**
     * Получить все документы дела.
     * Получение дочерних записей по родительской записи.
     *
     * @param caseId идентификатор дела
     * @return список документов дела
     */
    @Transactional(readOnly = true)
    public List<Document> getDocumentsByCase(Long caseId) {
        Case caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() -> new RuntimeException("Дело с ID " + caseId + " не найдено"));
        return documentRepository.findByCaseEntity(caseEntity);
    }

    /**
     * Получить документы дела по типу.
     *
     * @param caseId идентификатор дела
     * @param type тип документа
     * @return список документов
     */
    @Transactional(readOnly = true)
    public List<Document> getDocumentsByCaseAndType(Long caseId, DocumentType type) {
        Case caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() -> new RuntimeException("Дело с ID " + caseId + " не найдено"));
        return documentRepository.findByCaseEntityAndDocumentType(caseEntity, type);
    }

    /**
     * Создать новый документ.
     *
     * @param document новый документ
     * @return сохраненный документ
     */
    public Document createDocument(Document document) {
        // Проверяем наличие дела
        if (document.getCaseEntity() == null || document.getCaseEntity().getId() == null) {
            throw new RuntimeException("Дело должно быть указано");
        }

        // Проверяем существование дела
        if (!caseRepository.existsById(document.getCaseEntity().getId())) {
            throw new RuntimeException("Дело с ID " + document.getCaseEntity().getId() + " не найдено");
        }

        return documentRepository.save(document);
    }

    /**
     * Обновить существующий документ.
     *
     * @param id идентификатор документа
     * @param updatedDocument обновленные данные документа
     * @return обновленный документ
     */
    public Document updateDocument(Long id, Document updatedDocument) {
        Document existingDocument = getDocumentById(id);

        existingDocument.setCaseEntity(updatedDocument.getCaseEntity());
        existingDocument.setTitle(updatedDocument.getTitle());
        existingDocument.setDocumentType(updatedDocument.getDocumentType());
        existingDocument.setDescription(updatedDocument.getDescription());
        existingDocument.setFilePath(updatedDocument.getFilePath());
        existingDocument.setFileName(updatedDocument.getFileName());
        existingDocument.setFileSize(updatedDocument.getFileSize());
        existingDocument.setMimeType(updatedDocument.getMimeType());
        existingDocument.setDocumentDate(updatedDocument.getDocumentDate());
        existingDocument.setDocumentNumber(updatedDocument.getDocumentNumber());
        existingDocument.setIsImportant(updatedDocument.getIsImportant());
        existingDocument.setStatus(updatedDocument.getStatus());
        existingDocument.setNotes(updatedDocument.getNotes());

        return documentRepository.save(existingDocument);
    }

    /**
     * Удалить документ.
     *
     * @param id идентификатор документа
     */
    public void deleteDocument(Long id) {
        if (!documentRepository.existsById(id)) {
            throw new RuntimeException("Документ с ID " + id + " не найден");
        }
        documentRepository.deleteById(id);
    }

    /**
     * Изменить статус документа.
     *
     * @param id идентификатор документа
     * @param status новый статус
     * @return обновленный документ
     */
    public Document changeDocumentStatus(Long id, DocumentStatus status) {
        Document document = getDocumentById(id);
        document.setStatus(status);
        return documentRepository.save(document);
    }

    /**
     * Отметить документ как важный/неважный.
     *
     * @param id идентификатор документа
     * @param isImportant флаг важности
     * @return обновленный документ
     */
    public Document markAsImportant(Long id, boolean isImportant) {
        Document document = getDocumentById(id);
        document.setIsImportant(isImportant);
        return documentRepository.save(document);
    }

    /**
     * Получить важные документы дела.
     *
     * @param caseId идентификатор дела
     * @return список важных документов
     */
    @Transactional(readOnly = true)
    public List<Document> getImportantDocumentsByCase(Long caseId) {
        Case caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() -> new RuntimeException("Дело с ID " + caseId + " не найдено"));
        return documentRepository.findByCaseEntityAndIsImportant(caseEntity, true);
    }

    /**
     * Получить количество документов дела.
     *
     * @param caseId идентификатор дела
     * @return количество документов
     */
    @Transactional(readOnly = true)
    public long getDocumentCountByCase(Long caseId) {
        Case caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() -> new RuntimeException("Дело с ID " + caseId + " не найдено"));
        return documentRepository.countByCaseEntity(caseEntity);
    }
}