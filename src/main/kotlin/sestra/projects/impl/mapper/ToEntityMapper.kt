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

class ToEntityMapper {
    fun toEntity(project: Project, createdBy: String): ProjectEntity {
        return ProjectEntity().apply {
            name = project.name
            layers = project.layers
                .mapIndexed { idx, layer -> toEntity(idx, layer, this) }
                .toSet()
            this.createdBy = createdBy
        }
    }

    private fun toEntity(idx: Int, layer: Layer, holder: ProjectEntity): LayerEntity {
        return LayerEntity().apply {
            project = holder
            inProjectIndex = idx
            name = layer.name
            type = when (layer.type) {
                is SpanLayerType -> "span"
                is RelationLayerType -> "relation"
            }
            relationSpanRoles = when (layer.type) {
                is RelationLayerType ->
                    layer.type.spanRoles
                        .mapIndexed { index, spanRole -> toEntity(index, spanRole, this) }
                        .toSet()
                else -> emptySet()
            }
            attributes = layer.attrs
                .mapIndexed { idx, attr -> toEntity(idx, attr, this) }
                .toSet()
        }
    }

    private fun toEntity(idx: Int, spanRole: RelationLayerSpanRole, holder: LayerEntity): RelationLayerSpanRoleEntity {
        return RelationLayerSpanRoleEntity().apply {
            layer = holder
            inLayerIndex = idx
            name = spanRole.name
            targetLayerName = spanRole.targetLayerName
        }
    }

    private fun toEntity(idx: Int, attr: Attribute, holder: LayerEntity): AttributeEntity {
        return AttributeEntity().apply {
            layer = holder
            inLayerIndex = idx
            name = attr.name
            type = when (attr.type) {
                is BooleanAttributeType -> "boolean"
                is IntAttributeType -> "int"
                is FloatAttributeType -> "float"
                is StringAttributeType -> "string"
                is EnumAttributeType -> "enum"
            }
            enumValues = when (attr.type) {
                is EnumAttributeType ->
                    attr.type.values
                        .mapIndexed { idx, value -> toEntity(idx, value, this) }
                        .toSet()
                else -> emptySet()
            }
        }
    }

    private fun toEntity(idx: Int, value: String, holder: AttributeEntity): EnumAttributeValueEntity {
        return EnumAttributeValueEntity().apply {
            attribute = holder
            inAttributeIndex = idx
            name = value
        }
    }
}
