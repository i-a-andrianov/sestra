package sestra.documents.impl

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import sestra.documents.api.core.Document
import sestra.documents.api.store.CreateDocumentResult
import sestra.documents.api.store.DocumentAlreadyExists
import sestra.documents.api.store.DocumentCreated
import sestra.documents.api.store.DocumentsStore
import sestra.documents.api.store.GetDocumentsNamesResult
import sestra.documents.api.store.InvalidDocument
import sestra.documents.api.store.ProjectNotFound
import sestra.documents.impl.mapper.FromEntityMapper
import sestra.documents.impl.mapper.ToEntityMapper
import sestra.documents.impl.repository.DocumentRepository
import sestra.documents.impl.validator.DocumentValidator
import sestra.projects.api.store.ProjectsQueries

@Component
class DocumentsStoreImpl(
    private val projects: ProjectsQueries,
    private val repository: DocumentRepository
) : DocumentsStore {
    private val validator = DocumentValidator()
    private val toEntityMapper = ToEntityMapper()
    private val fromEntityMapper = FromEntityMapper()

    @Transactional
    override fun create(whoami: String, document: Document): CreateDocumentResult {
        val errors = validator.validate(document)
        if (errors.isNotEmpty()) {
            return InvalidDocument(errors)
        }

        val project = projects.getByName(whoami, document.projectName)
        if (project === null) {
            return ProjectNotFound
        }

        if (repository.existsByProjectNameAndName(document.projectName, document.name)) {
            return DocumentAlreadyExists
        }

        val entity = toEntityMapper.toEntity(document, whoami)
        repository.save(entity)
        return DocumentCreated
    }

    @Transactional(readOnly = true)
    override fun getByName(whoami: String, projectName: String, name: String): Document? {
        val entity = repository.findByProjectNameAndName(projectName, name)

        return when (entity) {
            null -> null
            else -> fromEntityMapper.fromEntity(entity)
        }
    }

    @Transactional(readOnly = true)
    override fun getNames(whoami: String, projectName: String): GetDocumentsNamesResult {
        val names = repository.findNamesByProjectName(projectName).map { it.name }
        return GetDocumentsNamesResult(names)
    }
}
