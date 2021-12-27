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
import sestra.projects.impl.annotations.entities.AnnotationAttributeEntity
import sestra.projects.impl.annotations.entities.AnnotationEntity
import sestra.projects.impl.annotations.entities.RelationAnnotationSpanRoleEntity

class AnnotationFromEntityMapper {
    fun fromEntity(annotation: AnnotationEntity): Annotation {
        return Annotation(
            id = annotation.uuid!!,
            value = when (annotation.type) {
                "span" -> SpanAnnotationValue(annotation.spanStart!!, annotation.spanEnd!!)
                "relation" -> RelationAnnotationValue(
                    spanRoles = annotation.relationSpanRoles!!
                        .sortedBy(RelationAnnotationSpanRoleEntity::inAnnotationIndex)
                        .map(this::fromEntity)
                )
                else -> throw IllegalStateException("Unknown annotation type '${annotation.type}'")
            },
            attrs = annotation.attrs!!
                .sortedBy(AnnotationAttributeEntity::inAnnotationIndex)
                .map(this::fromEntity)
        )
    }

    fun fromEntity(role: RelationAnnotationSpanRoleEntity): RelationAnnotationSpanRole {
        return RelationAnnotationSpanRole(
            name = role.name!!,
            targetAnnotationId = role.targetAnnotation!!.uuid!!
        )
    }

    fun fromEntity(attr: AnnotationAttributeEntity): AnnotationAttribute {
        return AnnotationAttribute(
            name = attr.name!!,
            value = when (attr.type) {
                "boolean" -> BooleanAttributeValue(attr.booleanValue!!)
                "int" -> IntAttributeValue(attr.intValue!!)
                "float" -> FloatAttributeValue(attr.floatValue!!)
                "string" -> StringAttributeValue(attr.stringValue!!)
                "enum" -> EnumAttributeValue(attr.enumValue!!)
                else -> throw IllegalStateException("Unknown annotation attribute type '${attr.type}'")
            }
        )
    }
}
