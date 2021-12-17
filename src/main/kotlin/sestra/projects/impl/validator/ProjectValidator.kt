package sestra.projects.impl.validator

import sestra.common.api.ValidationError
import sestra.projects.api.core.Layer
import sestra.projects.api.core.Project

class ProjectValidator {
    private val layerValidator = LayerValidator()

    fun validate(project: Project): List<ValidationError> {
        val result = mutableListOf<ValidationError>()

        if (project.name.isBlank()) {
            result += ValidationError("name", "should not be blank")
        }

        if (project.layers.isEmpty()) {
            result += ValidationError("layers", "should not be empty")
        }

        result += project.layers
            .groupBy(Layer::name)
            .filter { e -> e.value.size > 1 }
            .map { e -> ValidationError("layers", "value '${e.key}' is duplicated") }

        result += project.layers.flatMapIndexed { idx, layer ->
            layerValidator.validate(layer)
                .map { error -> error.copy(field = "layers[$idx].${error.field}") }
        }

        return result
    }
}
