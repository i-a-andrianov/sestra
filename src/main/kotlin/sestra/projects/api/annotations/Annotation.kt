package sestra.projects.api.annotations

import java.util.UUID

data class Annotation(
    val id: UUID,
    val value: AnnotationValue,
    val attrs: List<AnnotationAttribute>
)
