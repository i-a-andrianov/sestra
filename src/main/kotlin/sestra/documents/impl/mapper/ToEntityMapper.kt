package sestra.documents.impl.mapper

import sestra.documents.api.core.Document
import sestra.documents.impl.entities.DocumentEntity

class ToEntityMapper {
    fun toEntity(document: Document, createdBy: String): DocumentEntity {
        return DocumentEntity().apply {
            projectName = document.projectName
            name = document.name
            text = document.text
            this.createdBy = createdBy
        }
    }
}
