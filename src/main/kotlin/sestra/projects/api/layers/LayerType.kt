package sestra.projects.api.layers

sealed interface LayerType {
    object Span : LayerType

    data class Relation(
        val spanRoles: List<RelationLayerSpanRole>
    ) : LayerType
}
