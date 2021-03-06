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

class ProjectToEntityMapper {
    fun toEntity(project: Project, createdBy: String): ProjectEntity {
        val projectEnt = ProjectEntity()

        val layers = project.layers
            .mapIndexed { idx, layer -> Pair(layer, toEntity(idx, layer, projectEnt)) }

        val layersByName = layers.associate { (layer, layerEnt) -> Pair(layer.name, layerEnt) }

        layers.forEach { (layer, layerEnt) ->
            fillRelationSpanRoles(layerEnt, layer, layersByName)
        }

        return projectEnt.apply {
            name = project.name
            this.layers = layers
                .map { (_, layerEnt) -> layerEnt }
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
                is LayerType.Span -> "span"
                is LayerType.Relation -> "relation"
            }
            attributes = layer.attrs
                .mapIndexed { idx, attr -> toEntity(idx, attr, this) }
                .toSet()
        }
    }

    private fun fillRelationSpanRoles(layerEnt: LayerEntity, layer: Layer, layersByName: Map<String, LayerEntity>) {
        layerEnt.relationSpanRoles = when (layer.type) {
            is LayerType.Relation ->
                layer.type.spanRoles
                    .mapIndexed { index, spanRole ->
                        val targetLayer = layersByName[spanRole.targetLayerName]!!
                        toEntity(index, spanRole, layerEnt, targetLayer)
                    }
                    .toSet()
            else -> emptySet()
        }
    }

    private fun toEntity(
        idx: Int,
        spanRole: RelationLayerSpanRole,
        holder: LayerEntity,
        targetLayer: LayerEntity
    ): RelationLayerSpanRoleEntity {
        return RelationLayerSpanRoleEntity().apply {
            layer = holder
            inLayerIndex = idx
            name = spanRole.name
            this.targetLayer = targetLayer
        }
    }

    private fun toEntity(idx: Int, attr: Attribute, holder: LayerEntity): AttributeEntity {
        return AttributeEntity().apply {
            layer = holder
            inLayerIndex = idx
            name = attr.name
            type = when (attr.type) {
                is AttributeType.Boolean -> "boolean"
                is AttributeType.Int -> "int"
                is AttributeType.Float -> "float"
                is AttributeType.String -> "string"
                is AttributeType.Enum -> "enum"
            }
            enumValues = when (attr.type) {
                is AttributeType.Enum ->
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
