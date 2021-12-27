package sestra.projects.impl.projects.validator

import sestra.common.api.ValidationError
import sestra.projects.api.layers.Attribute
import sestra.projects.api.layers.Layer
import sestra.projects.api.layers.RelationLayerType
import sestra.projects.api.layers.SpanLayerType

class LayerValidator {
    private val attrValidator = AttributeValidator()

    fun validate(layer: Layer, layersByName: Map<String, Layer>): List<ValidationError> {
        val result = mutableListOf<ValidationError>()

        if (layer.name.isBlank()) {
            result += ValidationError("name", "should not be blank")
        }

        if (layer.type is RelationLayerType) {
            if (layer.type.spanRoles.size < 2) {
                result += ValidationError("type.spanRoles", "should have at least 2")
            }

            layer.type.spanRoles.forEachIndexed { idx, role ->
                result += validateSpanRole(
                    "type.spanRoles[$idx]",
                    role.name,
                    layersByName[role.targetLayerName]
                )
            }

            result += layer.type.spanRoles
                .groupBy { it.name }
                .filter { e -> e.value.size > 1 }
                .map { e -> ValidationError("type.spanRoles", "role '${e.key}' is duplicated") }
        }

        result += layer.attrs
            .groupBy(Attribute::name)
            .filter { e -> e.value.size > 1 }
            .map { e -> ValidationError("attrs", "name '${e.key}' is duplicated") }

        result += layer.attrs.flatMapIndexed { idx, attr ->
            attrValidator.validate(attr)
                .map { error -> error.copy(field = "attrs[$idx].${error.field}") }
        }

        return result
    }

    private fun validateSpanRole(fieldPrefix: String, name: String, targetLayer: Layer?): List<ValidationError> {
        val result = mutableListOf<ValidationError>()

        if (name.isBlank()) {
            result += ValidationError("$fieldPrefix.name", "should not be blank")
        }

        if (targetLayer === null) {
            result += ValidationError(
                "$fieldPrefix.targetLayerName",
                "should reference existing layer"
            )
        } else if (targetLayer.type !is SpanLayerType) {
            result += ValidationError(
                "$fieldPrefix.targetLayerName",
                "should reference span layer"
            )
        }

        return result
    }
}
