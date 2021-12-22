package sestra.documents.impl.mapper

import sestra.documents.api.core.Document
import sestra.documents.impl.entities.DocumentEntity

class FromEntityMapper {
    fun fromEntity(document: DocumentEntity): Document {
        return Document(
            projectName = document.projectName!!,
            name = document.name!!,
            text = document.text!!
        )
    }
}
