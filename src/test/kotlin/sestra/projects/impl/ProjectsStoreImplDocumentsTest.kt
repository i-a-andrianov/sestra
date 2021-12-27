package sestra.projects.impl

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import sestra.common.api.ValidationError
import sestra.projects.api.documents.Document
import sestra.projects.api.documents.DocumentAlreadyExists
import sestra.projects.api.documents.DocumentContainer
import sestra.projects.api.documents.DocumentCreated
import sestra.projects.api.documents.GetDocumentsNamesResult
import sestra.projects.api.documents.InvalidDocument
import sestra.projects.api.documents.ProjectNotFound
import sestra.projects.api.layers.Layer
import sestra.projects.api.layers.SpanLayerType
import sestra.projects.api.projects.Project

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(ProjectsStoreImpl::class)
class ProjectsStoreImplDocumentsTest {
    @Autowired
    private lateinit var store: ProjectsStoreImpl

    private val whoami = "admin"

    private val project = Project(
        name = "project1",
        layers = listOf(
            Layer(
                name = "layer1",
                type = SpanLayerType,
                attrs = emptyList()
            )
        )
    )

    private val container = DocumentContainer(
        projectName = project.name
    )

    private val document = Document(
        name = "doc1",
        text = "Hello, world!"
    )

    @Test
    fun `should return nothing on empty database`() {
        assertEquals(
            GetDocumentsNamesResult(emptyList()),
            store.getDocumentNames(whoami, container)
        )
        assertNull(
            store.getDocumentByName(whoami, container, document.name)
        )
    }

    @Test
    fun `should return nothing on empty project`() {
        store.createProject(whoami, project)

        assertEquals(
            GetDocumentsNamesResult(emptyList()),
            store.getDocumentNames(whoami, container)
        )
        assertNull(
            store.getDocumentByName(whoami, container, document.name)
        )
    }

    @Test
    fun `created document should become visible with the same data`() {
        store.createProject(whoami, project)
        val result = store.createDocument(whoami, container, document)

        assertEquals(DocumentCreated, result)
        assertEquals(
            GetDocumentsNamesResult(listOf(document.name)),
            store.getDocumentNames(whoami, container)
        )
        assertEquals(
            document,
            store.getDocumentByName(whoami, container, document.name)
        )
    }

    @Test
    fun `should return invalid document if so`() {
        store.createProject(whoami, project)
        val result = store.createDocument(whoami, container, document.copy(text = "    "))

        assertTrue(result is InvalidDocument)
        assertEquals(
            setOf("text"),
            (result as InvalidDocument).errors.map(ValidationError::field).toSet()
        )
        assertEquals(
            GetDocumentsNamesResult(emptyList()),
            store.getDocumentNames(whoami, container)
        )
        assertNull(
            store.getDocumentByName(whoami, container, document.name)
        )
    }

    @Test
    fun `should return project not found if so`() {
        val result = store.createDocument(whoami, container, document)

        assertEquals(ProjectNotFound, result)
        assertEquals(
            GetDocumentsNamesResult(emptyList()),
            store.getDocumentNames(whoami, container)
        )
        assertNull(
            store.getDocumentByName(whoami, container, document.name)
        )
    }

    @Test
    fun `should return project already exists if so`() {
        store.createProject(whoami, project)
        store.createDocument(whoami, container, document)

        val result = store.createDocument(whoami, container, document.copy(text = "haha"))

        assertEquals(DocumentAlreadyExists, result)
        assertEquals(
            GetDocumentsNamesResult(listOf(document.name)),
            store.getDocumentNames(whoami, container)
        )
        assertEquals(
            document,
            store.getDocumentByName(whoami, container, document.name)
        )
    }
}
