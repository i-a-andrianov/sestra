package sestra.projects.impl.documents

import sestra.projects.api.documents.CreateDocumentResult
import sestra.projects.api.documents.Document
import sestra.projects.api.documents.GetDocumentsNamesResult
import sestra.projects.impl.documents.mapper.DocumentFromEntityMapper
import sestra.projects.impl.documents.mapper.DocumentToEntityMapper
import sestra.projects.impl.documents.repository.DocumentsRepository
import sestra.projects.impl.documents.validator.DocumentValidator

class DocumentsCrud(
    private val repo: DocumentsRepository
) {
    private val validator = DocumentValidator()
    private val toMapper = DocumentToEntityMapper()
    private val fromMapper = DocumentFromEntityMapper()

    fun create(document: Document, projectId: Int, createdBy: String): CreateDocumentResult {
        val errors = validator.validate(document)
        if (errors.isNotEmpty()) {
            return CreateDocumentResult.InvalidDocument(errors)
        }

        if (repo.existsByProjectIdAndName(projectId, document.name)) {
            return CreateDocumentResult.DocumentAlreadyExists
        }

        val entity = toMapper.toEntity(document, projectId, createdBy)
        repo.save(entity)
        return CreateDocumentResult.DocumentCreated
    }

    fun getWithIdByName(projectId: Int, name: String): Pair<Int, Document>? {
        val entity = repo.findByProjectIdAndName(projectId, name)
        if (entity === null) {
            return null
        }
        return Pair(entity.id!!, fromMapper.fromEntity(entity))
    }

    fun getIdByName(projectId: Int, name: String): Int? {
        return repo.findIdByProjectIdAndName(projectId, name)?.id
    }

    fun getByName(projectId: Int, name: String): Document? {
        val entity = repo.findByProjectIdAndName(projectId, name)

        return when (entity) {
            null -> null
            else -> fromMapper.fromEntity(entity)
        }
    }

    fun getNames(projectId: Int): GetDocumentsNamesResult {
        val names = repo.findNamesByProjectId(projectId).map { it.name }
        return GetDocumentsNamesResult(names)
    }
}
