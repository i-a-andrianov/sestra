package sestra.documents.rest

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import sestra.common.api.ValidationError
import sestra.common.rest.WebSecurityConfig
import sestra.documents.api.core.Document
import sestra.documents.api.store.DocumentAlreadyExists
import sestra.documents.api.store.DocumentCreated
import sestra.documents.api.store.DocumentsStore
import sestra.documents.api.store.GetDocumentsNamesResult
import sestra.documents.api.store.InvalidDocument

@WebMvcTest(controllers = [DocumentsStoreRestController::class])
@Import(WebSecurityConfig::class)
class DocumentsStoreRestControllerTest {
    @MockBean
    private lateinit var store: DocumentsStore

    @Autowired
    private lateinit var client: MockMvc

    private val document = Document(
        projectName = "project1",
        name = "doc1",
        text = "text1"
    )

    private val documentJSON =
        """
            {
                "projectName": "project1",
                "name": "doc1",
                "text": "text1"
            }
        """.trimIndent()

    @Nested
    inner class Create {
        @Test
        fun `should return 401 for no creds`() {
            client.post("/api/documents") {
                accept = MediaType.APPLICATION_JSON
            }.andExpect {
                status { isUnauthorized() }
            }
        }

        @Test
        fun `should return 401 for incorrect creds`() {
            client.post("/api/documents") {
                accept = MediaType.APPLICATION_JSON
                headers {
                    setBasicAuth("admin", "admin")
                }
            }.andExpect {
                status { isUnauthorized() }
            }
        }

        @Test
        fun `should return 400 for incorrect payload`() {
            client.post("/api/documents") {
                accept = MediaType.APPLICATION_JSON
                headers {
                    setBasicAuth("admin", "sestra")
                }
                contentType = MediaType.APPLICATION_JSON
                content = "{}"
            }.andExpect {
                status { isBadRequest() }
            }
        }

        @Test
        fun `should return 400 for invalid document`() {
            val document = Document(
                projectName = "project1",
                name = "doc1",
                text = ""
            )
            Mockito.`when`(store.create("admin", document))
                .thenReturn(
                    InvalidDocument(
                        errors = listOf(
                            ValidationError("text", "should not be blank")
                        )
                    )
                )

            client.post("/api/documents") {
                accept = MediaType.APPLICATION_JSON
                headers {
                    setBasicAuth("admin", "sestra")
                }
                contentType = MediaType.APPLICATION_JSON
                content = """
                    {
                        "projectName": "project1",
                        "name": "doc1",
                        "text": ""
                    }
                """.trimIndent()
            }.andExpect {
                status { isBadRequest() }
                content {
                    contentType(MediaType.APPLICATION_JSON)
                    json(
                        """
                            {
                                "errors": [
                                    {
                                        "field": "text",
                                        "description": "should not be blank"
                                    }
                                ]
                            }
                        """.trimIndent()
                    )
                }
            }
        }

        @Test
        fun `should return 400 for document which already exists`() {
            Mockito.`when`(store.create("admin", document))
                .thenReturn(DocumentAlreadyExists)

            client.post("/api/documents") {
                accept = MediaType.APPLICATION_JSON
                headers {
                    setBasicAuth("admin", "sestra")
                }
                contentType = MediaType.APPLICATION_JSON
                content = documentJSON
            }.andExpect {
                status { isBadRequest() }
            }
        }

        @Test
        fun `should return 200 for correct document`() {
            Mockito.`when`(store.create("admin", document))
                .thenReturn(DocumentCreated)

            client.post("/api/documents") {
                accept = MediaType.APPLICATION_JSON
                headers {
                    setBasicAuth("admin", "sestra")
                }
                contentType = MediaType.APPLICATION_JSON
                content = documentJSON
            }.andExpect {
                status { isOk() }
            }
        }
    }

    @Nested
    inner class GetByName {
        @Test
        fun `should return 401 for no creds`() {
            client.get("/api/documents?projectName=project1&name=doc1") {
                accept = MediaType.APPLICATION_JSON
            }.andExpect {
                status { isUnauthorized() }
            }
        }

        @Test
        fun `should return 401 for incorrect creds`() {
            client.get("/api/documents?projectName=project1&name=doc1") {
                accept = MediaType.APPLICATION_JSON
                headers {
                    setBasicAuth("admin", "admin")
                }
            }.andExpect {
                status { isUnauthorized() }
            }
        }

        @Test
        fun `should return 400 when no projectName and name given`() {
            client.get("/api/documents") {
                accept = MediaType.APPLICATION_JSON
                headers {
                    setBasicAuth("admin", "sestra")
                }
            }.andExpect {
                status { isBadRequest() }
            }
        }

        @Test
        fun `should return 404 for unknown document`() {
            Mockito.`when`(store.getByName("admin", "project1", "doc1"))
                .thenReturn(null)

            client.get("/api/documents?projectName=project1&name=doc1") {
                accept = MediaType.APPLICATION_JSON
                headers {
                    setBasicAuth("admin", "sestra")
                }
            }.andExpect {
                status { isNotFound() }
            }
        }

        @Test
        fun `should return 200 and provide document with given name in JSON`() {
            Mockito.`when`(store.getByName("admin", "project1", "doc1"))
                .thenReturn(document)

            client.get("/api/documents?projectName=project1&name=doc1") {
                accept = MediaType.APPLICATION_JSON
                headers {
                    setBasicAuth("admin", "sestra")
                }
            }.andExpect {
                status { isOk() }
                content {
                    contentType(MediaType.APPLICATION_JSON)
                    json(documentJSON)
                }
            }
        }
    }

    @Nested
    inner class GetNames {
        @Test
        fun `should return 401 for no creds`() {
            client.get("/api/documents/names?projectName=project1") {
                accept = MediaType.APPLICATION_JSON
            }.andExpect {
                status { isUnauthorized() }
            }
        }

        @Test
        fun `should return 401 for incorrect creds`() {
            client.get("/api/documents/names?projectName=project1") {
                accept = MediaType.APPLICATION_JSON
                headers {
                    setBasicAuth("admin", "admin")
                }
            }.andExpect {
                status { isUnauthorized() }
            }
        }

        @Test
        fun `should return 400 when no projectName given`() {
            client.get("/api/documents/names") {
                accept = MediaType.APPLICATION_JSON
                headers {
                    setBasicAuth("admin", "sestra")
                }
            }.andExpect {
                status { isBadRequest() }
            }
        }

        @Test
        fun `should return 200 and provide given projects names in JSON`() {
            Mockito.`when`(store.getNames("admin", "project1"))
                .thenReturn(GetDocumentsNamesResult(listOf("doc1", "doc2")))

            client.get("/api/documents/names?projectName=project1") {
                accept = MediaType.APPLICATION_JSON
                headers {
                    setBasicAuth("admin", "sestra")
                }
            }.andExpect {
                status { isOk() }
                content {
                    contentType(MediaType.APPLICATION_JSON)
                    json(
                        """
                            {
                                "names": [
                                    "doc1",
                                    "doc2"
                                ]
                            }
                        """.trimIndent()
                    )
                }
            }
        }
    }
}
