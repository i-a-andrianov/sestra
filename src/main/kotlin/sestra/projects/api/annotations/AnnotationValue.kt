package sestra.projects.api.annotations

sealed interface AnnotationValue

data class SpanAnnotationValue(
    val start: Int,
    val end: Int
) : AnnotationValue

data class RelationAnnotationValue(
    val spanRoles: List<RelationAnnotationSpanRole>
) : AnnotationValue
