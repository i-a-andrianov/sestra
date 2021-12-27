package sestra.projects.impl.documents.mapper

import sestra.projects.api.documents.Document
import sestra.projects.impl.documents.entities.DocumentEntity

class DocumentToEntityMapper {
    fun toEntity(document: Document, projectId: Int, createdBy: String): DocumentEntity {
        return DocumentEntity().apply {
            this.projectId = projectId
            name = document.name
            text = document.text
            this.createdBy = createdBy
        }
    }
}
