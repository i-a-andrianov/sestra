package sestra.projects.impl.validator

import sestra.common.api.ValidationError
import sestra.projects.api.core.Attribute
import sestra.projects.api.core.Layer
import sestra.projects.api.core.RelationLayerType

class LayerValidator {
    private val attrValidator = AttributeValidator()

    fun validate(layer: Layer): List<ValidationError> {
        val result = mutableListOf<ValidationError>()

        if (layer.name.isBlank()) {
            result += ValidationError("name", "should not be blank")
        }

        if (layer.type is RelationLayerType) {
            if (layer.type.spanRoles.size < 2) {
                result += ValidationError("type.spanRoles", "should have at least 2")
            }

            layer.type.spanRoles.forEachIndexed { idx, role ->
                if (role.isBlank()) {
                    result += ValidationError("type.spanRoles[$idx]", "should not be blank")
                }
            }

            result += layer.type.spanRoles
                .groupBy { it }
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
}
