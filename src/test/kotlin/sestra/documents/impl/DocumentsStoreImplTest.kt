package sestra.documents.impl

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import sestra.common.api.ValidationError
import sestra.documents.api.core.Document
import sestra.documents.api.store.DocumentAlreadyExists
import sestra.documents.api.store.DocumentCreated
import sestra.documents.api.store.GetDocumentsNamesResult
import sestra.documents.api.store.InvalidDocument
import sestra.documents.api.store.ProjectNotFound
import sestra.projects.api.core.Project
import sestra.projects.api.store.ProjectsStore

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(DocumentsStoreImpl::class)
class DocumentsStoreImplTest {
    @Autowired
    private lateinit var store: DocumentsStoreImpl

    @MockBean
    private lateinit var projects: ProjectsStore

    private val whoami = "admin"

    private val document = Document(
        projectName = "project1",
        name = "doc1",
        text = "Hello, world!"
    )

    private val project = Project(
        name = document.projectName,
        layers = emptyList()
    )

    @Test
    fun `should return nothing on empty database`() {
        assertEquals(
            GetDocumentsNamesResult(emptyList()),
            store.getNames(whoami, document.projectName)
        )
        assertNull(
            store.getByName(whoami, document.projectName, document.name)
        )
    }

    @Test
    fun `created document should become visible with the same data`() {
        Mockito.`when`(projects.getByName(whoami, document.projectName))
            .thenReturn(project)

        val result = store.create(whoami, document)

        assertEquals(DocumentCreated, result)
        assertEquals(
            GetDocumentsNamesResult(listOf(document.name)),
            store.getNames(whoami, document.projectName)
        )
        assertEquals(
            document,
            store.getByName(whoami, document.projectName, document.name)
        )
    }

    @Test
    fun `should return invalid document if so`() {
        val result = store.create(whoami, document.copy(text = "    "))

        assertTrue(result is InvalidDocument)
        assertEquals(
            setOf("text"),
            (result as InvalidDocument).errors.map(ValidationError::field).toSet()
        )
        assertEquals(
            GetDocumentsNamesResult(emptyList()),
            store.getNames(whoami, document.projectName)
        )
        assertNull(
            store.getByName(whoami, document.projectName, document.name)
        )
    }

    @Test
    fun `should return project not found if so`() {
        Mockito.`when`(projects.getByName(whoami, document.projectName))
            .thenReturn(null)

        val result = store.create(whoami, document)

        assertEquals(ProjectNotFound, result)
        assertEquals(
            GetDocumentsNamesResult(emptyList()),
            store.getNames(whoami, document.projectName)
        )
        assertNull(
            store.getByName(whoami, document.projectName, document.name)
        )
    }

    @Test
    fun `should return project already exists if so`() {
        Mockito.`when`(projects.getByName(whoami, document.projectName))
            .thenReturn(project)

        store.create(whoami, document)

        val result = store.create(whoami, document.copy(text = "haha"))

        assertEquals(DocumentAlreadyExists, result)
        assertEquals(
            GetDocumentsNamesResult(listOf(document.name)),
            store.getNames(whoami, document.projectName)
        )
        assertEquals(
            document,
            store.getByName(whoami, document.projectName, document.name)
        )
    }
}
