package sestra.projects.impl.projects.validator

import sestra.common.api.ValidationError
import sestra.projects.api.layers.Attribute
import sestra.projects.api.layers.AttributeType

class AttributeValidator {
    fun validate(attr: Attribute): List<ValidationError> {
        val result = mutableListOf<ValidationError>()

        if (attr.name.isBlank()) {
            result += ValidationError("name", "should not be blank")
        }

        if (attr.type is AttributeType.Enum) {
            if (attr.type.values.size < 2) {
                result += ValidationError("type.values", "should have at least 2")
            }

            attr.type.values.forEachIndexed { idx, value ->
                if (value.isBlank()) {
                    result += ValidationError("type.values[$idx]", "should not be blank")
                }
            }

            result += attr.type.values
                .groupBy { it }
                .filter { e -> e.value.size > 1 }
                .map { e -> ValidationError("type.values", "value '${e.key}' is duplicated") }
        }

        return result
    }
}
