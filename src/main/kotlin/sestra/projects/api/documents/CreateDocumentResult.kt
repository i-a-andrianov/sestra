package sestra.projects.api.documents

import sestra.common.api.ValidationError

sealed interface CreateDocumentResult

object DocumentCreated : CreateDocumentResult

object DocumentAlreadyExists : CreateDocumentResult

object ProjectNotFound : CreateDocumentResult

data class InvalidDocument(
    val errors: List<ValidationError>
) : CreateDocumentResult
