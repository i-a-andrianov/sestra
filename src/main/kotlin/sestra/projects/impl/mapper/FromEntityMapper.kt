package sestra.projects.impl.mapper

import sestra.projects.api.core.Attribute
import sestra.projects.api.core.BooleanAttributeType
import sestra.projects.api.core.EnumAttributeType
import sestra.projects.api.core.FloatAttributeType
import sestra.projects.api.core.IntAttributeType
import sestra.projects.api.core.Layer
import sestra.projects.api.core.Project
import sestra.projects.api.core.RelationLayerSpanRole
import sestra.projects.api.core.RelationLayerType
import sestra.projects.api.core.SpanLayerType
import sestra.projects.api.core.StringAttributeType
import sestra.projects.impl.entities.AttributeEntity
import sestra.projects.impl.entities.EnumAttributeValueEntity
import sestra.projects.impl.entities.LayerEntity
import sestra.projects.impl.entities.ProjectEntity
import sestra.projects.impl.entities.RelationLayerSpanRoleEntity

class FromEntityMapper {
    fun fromEntity(project: ProjectEntity): Project {
        return Project(
            name = project.name!!,
            layers = project.layers!!
                .sortedBy(LayerEntity::inProjectIndex)
                .map { layer -> fromEntity(layer) }
        )
    }

    private fun fromEntity(layer: LayerEntity): Layer {
        return Layer(
            name = layer.name!!,
            type = when (layer.type) {
                "span" -> SpanLayerType
                "relation" -> RelationLayerType(
                    spanRoles = layer.relationSpanRoles!!
                        .sortedBy(RelationLayerSpanRoleEntity::inLayerIndex)
                        .map { spanRole ->
                            RelationLayerSpanRole(
                                name = spanRole.name!!,
                                targetLayerName = spanRole.targetLayerName!!
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
                "boolean" -> BooleanAttributeType
                "int" -> IntAttributeType
                "float" -> FloatAttributeType
                "string" -> StringAttributeType
                "enum" -> EnumAttributeType(
                    values = attr.enumValues!!
                        .sortedBy(EnumAttributeValueEntity::inAttributeIndex)
                        .map { value -> value.name!! }
                )
                else -> throw IllegalStateException("Unknown attribute type '${attr.type}'")
            }
        )
    }
}
