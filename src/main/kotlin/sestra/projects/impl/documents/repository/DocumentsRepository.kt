package sestra.projects.impl.documents.repository

import org.springframework.data.repository.CrudRepository
import sestra.projects.impl.documents.entities.DocumentEntity
import sestra.projects.impl.documents.entities.DocumentNameOnly

interface DocumentsRepository : CrudRepository<DocumentEntity, Int> {
    fun existsByProjectIdAndName(projectId: Int, name: String): Boolean

    fun findByProjectIdAndName(projectId: Int, name: String): DocumentEntity?

    fun findNamesByProjectId(projectId: Int): List<DocumentNameOnly>
}
