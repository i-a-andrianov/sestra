package sestra.projects.impl.annotations.validator

import sestra.common.api.ValidationError
import sestra.projects.api.annotations.Annotation
import sestra.projects.api.annotations.AnnotationAttribute
import sestra.projects.api.annotations.AnnotationValue
import sestra.projects.api.annotations.AttributeValue
import sestra.projects.api.documents.Document
import sestra.projects.api.layers.Attribute
import sestra.projects.api.layers.AttributeType
import sestra.projects.api.layers.Layer
import sestra.projects.api.layers.LayerType
import java.util.UUID

class AnnotationValidator {
    fun validate(
        annotation: Annotation,
        targetLayer: Layer,
        targetDocument: Document,
        existsAnnotationByLayerNameAndId: (String, UUID) -> Boolean
    ): List<ValidationError> {
        val result = mutableListOf<ValidationError>()

        val value = annotation.value

        result += when (value) {
            is AnnotationValue.Span -> validateSpanValue(value, targetDocument.text, targetLayer.type)
            is AnnotationValue.Relation -> validateRelationValue(
                value,
                existsAnnotationByLayerNameAndId,
                targetLayer.type
            )
        }

        result += validateAttrs(annotation.attrs, targetLayer.attrs)

        return result
    }

    private fun validateSpanValue(
        value: AnnotationValue.Span,
        text: String,
        layerType: LayerType
    ): List<ValidationError> {
        val result = mutableListOf<ValidationError>()

        if (layerType !is LayerType.Span) {
            result += ValidationError("value", "is span while target layer isn't")
        }

        if (value.start < 0) {
            result += ValidationError("value.start", "should be non-negative")
        }

        if (value.end <= value.start) {
            result += ValidationError("value.end", "should be greater than start")
        }

        if (value.end > text.length) {
            result += ValidationError("value.end", "should not be greater than document length")
        }

        return result
    }

    private fun validateRelationValue(
        value: AnnotationValue.Relation,
        existsAnnotationByLayerNameAndId: (String, UUID) -> Boolean,
        layerType: LayerType
    ): List<ValidationError> {
        val result = mutableListOf<ValidationError>()

        val valueRolesByName = value.spanRoles.groupBy { role -> role.name }

        valueRolesByName.forEach { (name, roles) ->
            if (roles.size > 1) {
                result += ValidationError("value.spanRoles", "name '$name' is duplicated")
            }
        }

        if (layerType !is LayerType.Relation) {
            result += ValidationError("value", "is relation while target layer isn't")
            return result
        }

        val valueRoles = valueRolesByName.keys
        val layerRoles = layerType.spanRoles.map { role -> role.name }.toSet()

        val unknownRoles = valueRoles - layerRoles
        if (unknownRoles.isNotEmpty()) {
            result += ValidationError(
                "value.spanRoles",
                "roles '$unknownRoles' are not defined in layer"
            )
        }

        val missingRoles = layerRoles - valueRoles
        if (missingRoles.isNotEmpty()) {
            result += ValidationError(
                "value.spanRoles",
                "roles '$missingRoles' defined in layer are missing"
            )
        }

        val layerRoleByName = layerType.spanRoles.associateBy { role -> role.name }
        value.spanRoles.forEachIndexed { idx, role ->
            val layerRole = layerRoleByName[role.name]

            val id = role.targetAnnotationId
            if (layerRole !== null && !existsAnnotationByLayerNameAndId(layerRole.targetLayerName, id)) {
                result += ValidationError(
                    "value.spanRoles[$idx].targetAnnotationId",
                    "annotation doesn't exist or is in incorrect layer"
                )
            }
        }

        return result
    }

    private fun validateAttrs(attrs: List<AnnotationAttribute>, layerAttrs: List<Attribute>): List<ValidationError> {
        val result = mutableListOf<ValidationError>()

        val attrsByName = attrs.groupBy { attr -> attr.name }

        attrsByName.forEach { (name, attrsForName) ->
            if (attrsForName.size > 1) {
                result += ValidationError("attrs", "name '$name' is duplicated")
            }
        }

        val attrsNames = attrsByName.keys
        val layerAttrsNames = layerAttrs.map { attr -> attr.name }.toSet()

        val unknownAttrs = attrsNames - layerAttrsNames
        if (unknownAttrs.isNotEmpty()) {
            result += ValidationError("attrs", "'$unknownAttrs' are not defined in layer")
        }

        val missingAttrs = layerAttrsNames - attrsNames
        if (missingAttrs.isNotEmpty()) {
            result += ValidationError("attrs", "'$missingAttrs' defined in layer are missing")
        }

        val layerAttrsByName = layerAttrs.associateBy { attr -> attr.name }

        attrs.forEachIndexed { idx, attr ->
            val layerAttr = layerAttrsByName[attr.name]
            if (layerAttr !== null) {
                result += validateAttr(idx, attr.value, layerAttr.type)
            }
        }

        return result
    }

    private fun validateAttr(idx: Int, value: AttributeValue, type: AttributeType): List<ValidationError> {
        val result = mutableListOf<ValidationError>()

        val compatibleTypes = when (value) {
            is AttributeValue.Boolean -> type is AttributeType.Boolean
            is AttributeValue.Int -> type is AttributeType.Int
            is AttributeValue.Float -> type is AttributeType.Float
            is AttributeValue.String -> type is AttributeType.String
            is AttributeValue.Enum -> type is AttributeType.Enum
        }

        if (!compatibleTypes) {
            result += ValidationError("attrs[$idx].value", "incompatible type with layer")
        }

        if (value is AttributeValue.Enum && type is AttributeType.Enum && value.value !in type.values) {
            result += ValidationError("attrs[$idx].value", "unknown enum constant")
        }

        return result
    }
}
