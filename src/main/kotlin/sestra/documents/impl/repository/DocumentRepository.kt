package sestra.documents.impl.repository

import org.springframework.data.repository.CrudRepository
import sestra.documents.impl.entities.DocumentEntity
import sestra.documents.impl.entities.DocumentNameOnly

interface DocumentRepository : CrudRepository<DocumentEntity, Int> {
    fun existsByProjectNameAndName(projectName: String, name: String): Boolean

    fun findByProjectNameAndName(projectName: String, name: String): DocumentEntity?

    fun findNamesByProjectName(projectName: String): List<DocumentNameOnly>
}
