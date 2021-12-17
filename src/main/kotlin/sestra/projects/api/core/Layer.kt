package sestra.projects.api.core

data class Layer(
    val name: String,
    val type: LayerType,
    val attrs: List<Attribute>
)
