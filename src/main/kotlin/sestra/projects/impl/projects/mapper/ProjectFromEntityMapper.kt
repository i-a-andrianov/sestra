package sestra.projects.impl.projects.mapper

import sestra.projects.api.layers.Attribute
import sestra.projects.api.layers.AttributeType
import sestra.projects.api.layers.Layer
import sestra.projects.api.layers.LayerType
import sestra.projects.api.layers.RelationLayerSpanRole
import sestra.projects.api.projects.Project
import sestra.projects.impl.projects.entities.AttributeEntity
import sestra.projects.impl.projects.entities.EnumAttributeValueEntity
import sestra.projects.impl.projects.entities.LayerEntity
import sestra.projects.impl.projects.entities.ProjectEntity
import sestra.projects.impl.projects.entities.RelationLayerSpanRoleEntity

class ProjectFromEntityMapper {
    fun fromEntity(project: ProjectEntity): Project {
        return Project(
            name = project.name!!,
            layers = project.layers!!
                .sortedBy(LayerEntity::inProjectIndex)
                .map { layer -> fromEntity(layer) }
        )
    }

    fun fromEntity(layer: LayerEntity): Layer {
        return Layer(
            name = layer.name!!,
            type = when (layer.type) {
                "span" -> LayerType.Span
                "relation" -> LayerType.Relation(
                    spanRoles = layer.relationSpanRoles!!
                        .sortedBy(RelationLayerSpanRoleEntity::inLayerIndex)
                        .map { spanRole ->
                            RelationLayerSpanRole(
                                name = spanRole.name!!,
                                targetLayerName = spanRole.targetLayer!!.name!!
                            )
                        }
                )
                else -> throw IllegalStateException("Unknown layer type '${layer.type}'")
            },
            attrs = layer.attributes!!
                .sortedBy(AttributeEntity::inLayerIndex)
                .map { attr -> fromEntity(attr) }
        )
    }

    private fun fromEntity(attr: AttributeEntity): Attribute {
        return Attribute(
            name = attr.name!!,
            type = when (attr.type) {
                "boolean" -> AttributeType.Boolean
                "int" -> AttributeType.Int
                "float" -> AttributeType.Float
                "string" -> AttributeType.String
                "enum" -> AttributeType.Enum(
                    values = attr.enumValues!!
                        .sortedBy(EnumAttributeValueEntity::inAttributeIndex)
                        .map { value -> value.name!! }
                )
                else -> throw IllegalStateException("Unknown attribute type '${attr.type}'")
            }
        )
    }
}
