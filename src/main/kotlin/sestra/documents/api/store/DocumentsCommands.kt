package sestra.documents.api.store

import sestra.common.api.ValidationError
import sestra.documents.api.core.Document

interface DocumentsCommands {
    fun create(whoami: String, document: Document): CreateDocumentResult
}

sealed interface CreateDocumentResult

object DocumentCreated : CreateDocumentResult

object DocumentAlreadyExists : CreateDocumentResult

object ProjectNotFound : CreateDocumentResult

data class InvalidDocument(
    val errors: List<ValidationError>
) : CreateDocumentResult
