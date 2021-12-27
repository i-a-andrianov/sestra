package sestra.projects.impl

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import sestra.projects.api.annotations.Annotation
import sestra.projects.api.annotations.AnnotationContainer
import sestra.projects.api.annotations.AnnotationsStore
import sestra.projects.api.annotations.CreateAnnotationResult
import sestra.projects.api.annotations.DeleteAnnotationResult
import sestra.projects.api.annotations.DocumentNotFound
import sestra.projects.api.annotations.LayerNotFound
import sestra.projects.api.documents.CreateDocumentResult
import sestra.projects.api.documents.Document
import sestra.projects.api.documents.DocumentContainer
import sestra.projects.api.documents.DocumentsStore
import sestra.projects.api.documents.GetDocumentsNamesResult
import sestra.projects.api.projects.CreateProjectResult
import sestra.projects.api.projects.GetProjectsNamesResult
import sestra.projects.api.projects.Project
import sestra.projects.api.projects.ProjectsStore
import sestra.projects.impl.annotations.AnnotationsCrud
import sestra.projects.impl.annotations.repository.AnnotationsRepository
import sestra.projects.impl.annotations.repository.RelationAnnotationSpanRolesRepository
import sestra.projects.impl.documents.DocumentsCrud
import sestra.projects.impl.documents.repository.DocumentsRepository
import sestra.projects.impl.projects.ProjectsCrud
import sestra.projects.impl.projects.repository.LayersRepository
import sestra.projects.impl.projects.repository.ProjectsRepository
import java.util.UUID
import sestra.projects.api.annotations.ProjectNotFound as AnnProjectNotFound
import sestra.projects.api.documents.ProjectNotFound as DocProjectNotFound

@Component
class ProjectsStoreImpl(
    projectsRepository: ProjectsRepository,
    layersRepository: LayersRepository,
    documentsRepository: DocumentsRepository,
    annotationsRepository: AnnotationsRepository,
    spanRolesRepository: RelationAnnotationSpanRolesRepository
) : ProjectsStore, DocumentsStore, AnnotationsStore {
    private val projectsCrud = ProjectsCrud(projectsRepository, layersRepository)
    private val documentsCrud = DocumentsCrud(documentsRepository)
    private val annotationsCrud = AnnotationsCrud(annotationsRepository, spanRolesRepository)

    @Transactional
    override fun createProject(whoami: String, project: Project): CreateProjectResult {
        return projectsCrud.create(project, whoami)
    }

    @Transactional(readOnly = true)
    override fun getProjectByName(whoami: String, name: String): Project? {
        return projectsCrud.getByName(name)
    }

    @Transactional(readOnly = true)
    override fun getProjectNames(whoami: String): GetProjectsNamesResult {
        return projectsCrud.getNames()
    }

    @Transactional
    override fun createDocument(
        whoami: String,
        container: DocumentContainer,
        document: Document
    ): CreateDocumentResult {
        val projectId = projectsCrud.getIdByName(container.projectName)
        if (projectId === null) {
            return DocProjectNotFound
        }
        return documentsCrud.create(document, projectId, whoami)
    }

    @Transactional(readOnly = true)
    override fun getDocumentByName(whoami: String, container: DocumentContainer, name: String): Document? {
        val projectId = projectsCrud.getIdByName(container.projectName)
        if (projectId === null) {
            return null
        }
        return documentsCrud.getByName(projectId, name)
    }

    @Transactional(readOnly = true)
    override fun getDocumentNames(whoami: String, container: DocumentContainer): GetDocumentsNamesResult {
        val projectId = projectsCrud.getIdByName(container.projectName)
        if (projectId === null) {
            return GetDocumentsNamesResult(emptyList())
        }
        return documentsCrud.getNames(projectId)
    }

    @Transactional
    override fun createAnnotation(
        whoami: String,
        container: AnnotationContainer,
        annotation: Annotation
    ): CreateAnnotationResult {
        val projectId = projectsCrud.getIdByName(container.projectName)
        if (projectId === null) {
            return AnnProjectNotFound
        }
        val documentWithId = documentsCrud.getWithIdByName(projectId, container.documentName)
        if (documentWithId === null) {
            return DocumentNotFound
        }
        val layerWithId = projectsCrud.getLayerWithIdByName(container.projectName, container.layerName)
        if (layerWithId === null) {
            return LayerNotFound
        }
        val (layerId, layer) = layerWithId
        val (documentId, document) = documentWithId
        return annotationsCrud.create(layerId, layer, documentId, document, annotation, whoami) { layerName ->
            projectsCrud.getLayerIdByName(container.projectName, layerName)
        }
    }

    @Transactional(readOnly = true)
    override fun getAnnotations(whoami: String, container: AnnotationContainer): List<Annotation> {
        val projectId = projectsCrud.getIdByName(container.projectName)
        if (projectId === null) {
            return emptyList()
        }
        val documentId = documentsCrud.getIdByName(projectId, container.documentName)
        if (documentId === null) {
            return emptyList()
        }
        val layerId = projectsCrud.getLayerIdByName(container.projectName, container.layerName)
        if (layerId === null) {
            return emptyList()
        }
        return annotationsCrud.getAll(layerId, documentId, whoami)
    }

    @Transactional
    override fun deleteAnnotation(whoami: String, id: UUID): DeleteAnnotationResult {
        return annotationsCrud.delete(id, whoami)
    }
}
