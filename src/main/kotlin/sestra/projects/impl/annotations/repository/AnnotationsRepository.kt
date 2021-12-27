package sestra.projects.impl.annotations.repository

import org.springframework.data.repository.CrudRepository
import sestra.projects.impl.annotations.entities.AnnotationEntity
import java.util.UUID

interface AnnotationsRepository : CrudRepository<AnnotationEntity, Int> {
    fun findAllByLayerIdAndDocumentIdAndCreatedBy(
        layerId: Int,
        documentId: Int,
        createdBy: String
    ): List<AnnotationEntity>

    fun findByUuidAndLayerIdAndDocumentIdAndCreatedBy(
        uuid: UUID,
        layerId: Int,
        documentId: Int,
        createdBy: String
    ): AnnotationEntity?

    fun existsByUuidAndLayerIdAndDocumentIdAndCreatedBy(
        uuid: UUID,
        layerId: Int,
        documentId: Int,
        createdBy: String
    ): Boolean

    fun existsByUuid(uuid: UUID): Boolean

    fun existsByUuidAndCreatedBy(uuid: UUID, createdBy: String): Boolean

    fun deleteByUuid(uuid: UUID)
}
