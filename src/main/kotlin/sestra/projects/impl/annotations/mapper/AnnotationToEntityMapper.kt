package sestra.projects.impl.annotations.mapper

import sestra.projects.api.annotations.Annotation
import sestra.projects.api.annotations.AnnotationAttribute
import sestra.projects.api.annotations.AnnotationValue
import sestra.projects.api.annotations.AttributeValue
import sestra.projects.api.annotations.RelationAnnotationSpanRole
import sestra.projects.api.layers.Layer
import sestra.projects.api.layers.LayerType
import sestra.projects.api.layers.RelationLayerSpanRole
import sestra.projects.impl.annotations.entities.AnnotationAttributeEntity
import sestra.projects.impl.annotations.entities.AnnotationEntity
import sestra.projects.impl.annotations.entities.RelationAnnotationSpanRoleEntity
import java.util.UUID

class AnnotationToEntityMapper {
    fun toEntity(
        annotation: Annotation,
        layerId: Int,
        layer: Layer,
        documentId: Int,
        createdBy: String,
        getAnnotationByLayerNameAndId: (String, UUID) -> AnnotationEntity?
    ): AnnotationEntity {
        return AnnotationEntity().apply {
            uuid = annotation.id
            this.documentId = documentId
            this.layerId = layerId
            type = when (annotation.value) {
                is AnnotationValue.Span -> "span"
                is AnnotationValue.Relation -> "relation"
            }
            spanStart = when (annotation.value) {
                is AnnotationValue.Span -> annotation.value.start
                else -> null
            }
            spanEnd = when (annotation.value) {
                is AnnotationValue.Span -> annotation.value.end
                else -> null
            }
            relationSpanRoles = when (annotation.value) {
                is AnnotationValue.Span -> emptySet()
                is AnnotationValue.Relation -> {
                    val roleByName = (layer.type as LayerType.Relation).spanRoles
                        .associateBy { role -> role.name }

                    annotation.value.spanRoles
                        .mapIndexed { idx, role ->
                            toEntity(idx, role, this, roleByName, getAnnotationByLayerNameAndId)
                        }
                        .toSet()
                }
            }
            attrs = annotation.attrs
                .mapIndexed { idx, attr -> toEntity(idx, attr, this) }
                .toSet()
            this.createdBy = createdBy
        }
    }

    private fun toEntity(
        idx: Int,
        role: RelationAnnotationSpanRole,
        holder: AnnotationEntity,
        roleByName: Map<String, RelationLayerSpanRole>,
        getAnnotationByLayerNameAndId: (String, UUID) -> AnnotationEntity?
    ): RelationAnnotationSpanRoleEntity {
        val targetLayerName = roleByName[role.name]!!.targetLayerName

        return RelationAnnotationSpanRoleEntity().apply {
            annotation = holder
            inAnnotationIndex = idx
            name = role.name
            targetAnnotation = getAnnotationByLayerNameAndId(targetLayerName, role.targetAnnotationId)!!
        }
    }

    private fun toEntity(idx: Int, attr: AnnotationAttribute, holder: AnnotationEntity): AnnotationAttributeEntity {
        return AnnotationAttributeEntity().apply {
            annotation = holder
            inAnnotationIndex = idx
            name = attr.name
            type = when (attr.value) {
                is AttributeValue.Boolean -> "boolean"
                is AttributeValue.Int -> "int"
                is AttributeValue.Float -> "float"
                is AttributeValue.String -> "string"
                is AttributeValue.Enum -> "enum"
            }
            booleanValue = when (attr.value) {
                is AttributeValue.Boolean -> attr.value.value
                else -> null
            }
            intValue = when (attr.value) {
                is AttributeValue.Int -> attr.value.value
                else -> null
            }
            floatValue = when (attr.value) {
                is AttributeValue.Float -> attr.value.value
                else -> null
            }
            stringValue = when (attr.value) {
                is AttributeValue.String -> attr.value.value
                else -> null
            }
            enumValue = when (attr.value) {
                is AttributeValue.Enum -> attr.value.value
                else -> null
            }
        }
    }
}
