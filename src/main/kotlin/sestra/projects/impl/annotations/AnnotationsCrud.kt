package sestra.projects.impl.annotations

import sestra.projects.api.annotations.Annotation
import sestra.projects.api.annotations.CreateAnnotationResult
import sestra.projects.api.annotations.DeleteAnnotationResult
import sestra.projects.api.documents.Document
import sestra.projects.api.layers.Layer
import sestra.projects.impl.annotations.mapper.AnnotationFromEntityMapper
import sestra.projects.impl.annotations.mapper.AnnotationToEntityMapper
import sestra.projects.impl.annotations.repository.AnnotationsRepository
import sestra.projects.impl.annotations.repository.RelationAnnotationSpanRolesRepository
import sestra.projects.impl.annotations.validator.AnnotationValidator
import java.util.UUID

class AnnotationsCrud(
    private val repo: AnnotationsRepository,
    private val spanRolesRepo: RelationAnnotationSpanRolesRepository
) {
    private val validator = AnnotationValidator()
    private val toMapper = AnnotationToEntityMapper()
    private val fromMapper = AnnotationFromEntityMapper()

    fun create(
        layerId: Int,
        layer: Layer,
        documentId: Int,
        document: Document,
        annotation: Annotation,
        createdBy: String,
        getLayerIdByName: (String) -> Int?
    ): CreateAnnotationResult {
        val errors = validator.validate(annotation, layer, document) { targetLayerName, id ->
            val targetLayerId = getLayerIdByName(targetLayerName)!!
            repo.existsByUuidAndLayerIdAndDocumentIdAndCreatedBy(id, targetLayerId, documentId, createdBy)
        }
        if (errors.isNotEmpty()) {
            return CreateAnnotationResult.InvalidAnnotation(errors)
        }

        if (repo.existsByUuid(annotation.id)) {
            return CreateAnnotationResult.AnnotationAlreadyExists
        }

        val entity = toMapper.toEntity(annotation, layerId, layer, documentId, createdBy) { targetLayerName, id ->
            val targetLayerId = getLayerIdByName(targetLayerName)!!
            repo.findByUuidAndLayerIdAndDocumentIdAndCreatedBy(id, targetLayerId, documentId, createdBy)
        }
        repo.save(entity)
        return CreateAnnotationResult.AnnotationCreated
    }

    fun getAll(layerId: Int, documentId: Int, createdBy: String): List<Annotation> {
        val entities = repo.findAllByLayerIdAndDocumentIdAndCreatedBy(layerId, documentId, createdBy)
        return entities.map(fromMapper::fromEntity)
    }

    fun delete(id: UUID, createdBy: String): DeleteAnnotationResult {
        if (!repo.existsByUuidAndCreatedBy(id, createdBy)) {
            return DeleteAnnotationResult.AnnotationNotFound
        }

        val entities = spanRolesRepo.findAllByTargetAnnotationUuid(id)
        // should deduplicate as many roles could reference the same annotation
        val ids = entities.map { e -> e.annotation!!.uuid!! }.toSet().toList()
        if (ids.isNotEmpty()) {
            return DeleteAnnotationResult.AnnotationIsReferencedByOthers(ids)
        }

        repo.deleteByUuid(id)
        return DeleteAnnotationResult.AnnotationDeleted
    }
}
