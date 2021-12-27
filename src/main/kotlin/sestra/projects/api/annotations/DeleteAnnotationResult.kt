package sestra.projects.api.annotations

import java.util.UUID

sealed interface DeleteAnnotationResult

object AnnotationDeleted : DeleteAnnotationResult

data class AnnotationIsReferencedByOthers(
    val ids: List<UUID>
) : DeleteAnnotationResult

object AnnotationNotFound : DeleteAnnotationResult
