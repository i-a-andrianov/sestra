package sestra.projects.api.core

sealed interface LayerType

object SpanLayerType : LayerType

data class RelationLayerType(
    val spanRoles: List<RelationLayerSpanRole>
) : LayerType
