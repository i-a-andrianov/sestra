package sestra.projects.api.annotations

sealed interface AnnotationValue {
    data class Span(
        val start: Int,
        val end: Int
    ) : AnnotationValue

    data class Relation(
        val spanRoles: List<RelationAnnotationSpanRole>
    ) : AnnotationValue
}
