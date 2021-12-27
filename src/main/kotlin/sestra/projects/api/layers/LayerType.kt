package sestra.projects.api.layers

sealed interface LayerType

object SpanLayerType : LayerType

data class RelationLayerType(
    val spanRoles: List<RelationLayerSpanRole>
) : LayerType
