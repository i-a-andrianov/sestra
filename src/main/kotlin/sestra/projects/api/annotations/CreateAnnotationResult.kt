package sestra.projects.api.annotations

import sestra.common.api.ValidationError

sealed interface CreateAnnotationResult

object AnnotationCreated : CreateAnnotationResult

object ProjectNotFound : CreateAnnotationResult

object LayerNotFound : CreateAnnotationResult

object DocumentNotFound : CreateAnnotationResult

object AnnotationAlreadyExists : CreateAnnotationResult

data class InvalidAnnotation(
    val errors: List<ValidationError>
) : CreateAnnotationResult
