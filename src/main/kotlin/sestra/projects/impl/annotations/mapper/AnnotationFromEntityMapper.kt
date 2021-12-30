package sestra.projects.impl.annotations.mapper

import sestra.projects.api.annotations.Annotation
import sestra.projects.api.annotations.AnnotationAttribute
import sestra.projects.api.annotations.AnnotationValue
import sestra.projects.api.annotations.AttributeValue
import sestra.projects.api.annotations.RelationAnnotationSpanRole
import sestra.projects.impl.annotations.entities.AnnotationAttributeEntity
import sestra.projects.impl.annotations.entities.AnnotationEntity
import sestra.projects.impl.annotations.entities.RelationAnnotationSpanRoleEntity

class AnnotationFromEntityMapper {
    fun fromEntity(annotation: AnnotationEntity): Annotation {
        return Annotation(
            id = annotation.uuid!!,
            value = when (annotation.type) {
                "span" -> AnnotationValue.Span(annotation.spanStart!!, annotation.spanEnd!!)
                "relation" -> AnnotationValue.Relation(
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
                "boolean" -> AttributeValue.Boolean(attr.booleanValue!!)
                "int" -> AttributeValue.Int(attr.intValue!!)
                "float" -> AttributeValue.Float(attr.floatValue!!)
                "string" -> AttributeValue.String(attr.stringValue!!)
                "enum" -> AttributeValue.Enum(attr.enumValue!!)
                else -> throw IllegalStateException("Unknown annotation attribute type '${attr.type}'")
            }
        )
    }
}
