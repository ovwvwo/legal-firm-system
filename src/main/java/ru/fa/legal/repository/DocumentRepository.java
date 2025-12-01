// ============================================
// DocumentRepository.java
// ============================================
package ru.fa.legal.repository;

// Импорт Spring Data JPA
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
// Импорт моделей
import ru.fa.legal.model.Document;
import ru.fa.legal.model.Case;
import ru.fa.legal.model.DocumentType;
import ru.fa.legal.model.DocumentStatus;

// Импорт классов для работы с коллекциями
import java.util.List;

/**
 * Репозиторий для работы с документами.
 * Обеспечивает доступ к данным документов в БД.
 * Реализует доступ к дочерним сущностям в связи "родитель-дочка".
 *
 * @author Иванов Егор Борисович
 * @version 1.0
 */
@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    /**
     * Найти все документы дела.
     * Получить все дочерние записи для родительской записи (дела).
     *
     * @param caseEntity дело (родительская сущность)
     * @return список документов дела
     */
    List<Document> findByCaseEntity(Case caseEntity);

    /**
     * Найти документы дела по типу.
     *
     * @param caseEntity дело
     * @param documentType тип документа
     * @return список документов
     */
    List<Document> findByCaseEntityAndDocumentType(Case caseEntity, DocumentType documentType);

    /**
     * Найти важные документы дела.
     *
     * @param caseEntity дело
     * @param isImportant флаг важности
     * @return список важных документов
     */
    List<Document> findByCaseEntityAndIsImportant(Case caseEntity, Boolean isImportant);

    /**
     * Найти документы по статусу.
     *
     * @param status статус документа
     * @return список документов
     */
    List<Document> findByStatus(DocumentStatus status);

    /**
     * Найти документы дела по статусу.
     *
     * @param caseEntity дело
     * @param status статус документа
     * @return список документов
     */
    List<Document> findByCaseEntityAndStatus(Case caseEntity, DocumentStatus status);

    /**
     * Подсчитать количество документов дела.
     *
     * @param caseEntity дело
     * @return количество документов
     */
    long countByCaseEntity(Case caseEntity);

    /**
     * Подсчитать количество важных документов дела.
     *
     * @param caseEntity дело
     * @param isImportant флаг важности
     * @return количество важных документов
     */
    long countByCaseEntityAndIsImportant(Case caseEntity, Boolean isImportant);
}
