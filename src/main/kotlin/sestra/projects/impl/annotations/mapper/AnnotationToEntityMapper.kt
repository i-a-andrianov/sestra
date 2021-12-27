package sestra.projects.impl.annotations.mapper

import sestra.projects.api.annotations.Annotation
import sestra.projects.api.annotations.AnnotationAttribute
import sestra.projects.api.annotations.BooleanAttributeValue
import sestra.projects.api.annotations.EnumAttributeValue
import sestra.projects.api.annotations.FloatAttributeValue
import sestra.projects.api.annotations.IntAttributeValue
import sestra.projects.api.annotations.RelationAnnotationSpanRole
import sestra.projects.api.annotations.RelationAnnotationValue
import sestra.projects.api.annotations.SpanAnnotationValue
import sestra.projects.api.annotations.StringAttributeValue
import sestra.projects.api.layers.Layer
import sestra.projects.api.layers.RelationLayerSpanRole
import sestra.projects.api.layers.RelationLayerType
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
                is SpanAnnotationValue -> "span"
                is RelationAnnotationValue -> "relation"
            }
            spanStart = when (annotation.value) {
                is SpanAnnotationValue -> annotation.value.start
                else -> null
            }
            spanEnd = when (annotation.value) {
                is SpanAnnotationValue -> annotation.value.end
                else -> null
            }
            relationSpanRoles = when (annotation.value) {
                is SpanAnnotationValue -> emptySet()
                is RelationAnnotationValue -> {
                    val roleByName = (layer.type as RelationLayerType).spanRoles
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
                is BooleanAttributeValue -> "boolean"
                is IntAttributeValue -> "int"
                is FloatAttributeValue -> "float"
                is StringAttributeValue -> "string"
                is EnumAttributeValue -> "enum"
            }
            booleanValue = when (attr.value) {
                is BooleanAttributeValue -> attr.value.value
                else -> null
            }
            intValue = when (attr.value) {
                is IntAttributeValue -> attr.value.value
                else -> null
            }
            floatValue = when (attr.value) {
                is FloatAttributeValue -> attr.value.value
                else -> null
            }
            stringValue = when (attr.value) {
                is StringAttributeValue -> attr.value.value
                else -> null
            }
            enumValue = when (attr.value) {
                is EnumAttributeValue -> attr.value.value
                else -> null
            }
        }
    }
}
