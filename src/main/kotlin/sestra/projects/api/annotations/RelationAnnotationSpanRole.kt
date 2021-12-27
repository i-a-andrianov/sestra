package sestra.projects.api.annotations

import java.util.UUID

data class RelationAnnotationSpanRole(
    val name: String,
    val targetAnnotationId: UUID
)
