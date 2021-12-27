package sestra.projects.impl

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import sestra.projects.api.documents.CreateDocumentResult
import sestra.projects.api.documents.Document
import sestra.projects.api.documents.DocumentContainer
import sestra.projects.api.documents.DocumentsStore
import sestra.projects.api.documents.GetDocumentsNamesResult
import sestra.projects.api.documents.ProjectNotFound
import sestra.projects.api.projects.CreateProjectResult
import sestra.projects.api.projects.GetProjectsNamesResult
import sestra.projects.api.projects.Project
import sestra.projects.api.projects.ProjectsStore
import sestra.projects.impl.documents.DocumentsCrud
import sestra.projects.impl.documents.repository.DocumentsRepository
import sestra.projects.impl.projects.ProjectsCrud
import sestra.projects.impl.projects.repository.ProjectsRepository

@Component
class ProjectsStoreImpl(
    projectsRepository: ProjectsRepository,
    documentsRepository: DocumentsRepository
) : ProjectsStore, DocumentsStore {
    private val projectsCrud = ProjectsCrud(projectsRepository)
    private val documentsCrud = DocumentsCrud(documentsRepository)

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
            return ProjectNotFound
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
}
