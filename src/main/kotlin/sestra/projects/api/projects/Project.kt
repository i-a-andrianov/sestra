package sestra.projects.api.projects

import sestra.projects.api.layers.Layer

data class Project(
    val name: String,
    val layers: List<Layer>
)
