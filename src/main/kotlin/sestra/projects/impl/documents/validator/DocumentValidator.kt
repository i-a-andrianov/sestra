package sestra.projects.impl.documents.validator

import sestra.common.api.ValidationError
import sestra.projects.api.documents.Document

class DocumentValidator {
    fun validate(document: Document): List<ValidationError> {
        val result = mutableListOf<ValidationError>()

        if (document.name.isBlank()) {
            result += ValidationError("name", "should not be blank")
        }

        if (document.text.isBlank()) {
            result += ValidationError("text", "should not be blank")
        }

        return result
    }
}
