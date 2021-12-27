package sestra.projects.api.layers

data class Layer(
    val name: String,
    val type: LayerType,
    val attrs: List<Attribute>
)
