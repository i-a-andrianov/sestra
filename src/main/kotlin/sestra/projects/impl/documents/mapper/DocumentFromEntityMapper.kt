package sestra.projects.impl.documents.mapper

import sestra.projects.api.documents.Document
import sestra.projects.impl.documents.entities.DocumentEntity

class DocumentFromEntityMapper {
    fun fromEntity(document: DocumentEntity): Document {
        return Document(
            name = document.name!!,
            text = document.text!!
        )
    }
}
