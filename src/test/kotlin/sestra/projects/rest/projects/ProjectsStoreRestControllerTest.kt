package sestra.projects.rest.projects

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
import sestra.projects.api.layers.Attribute
import sestra.projects.api.layers.BooleanAttributeType
import sestra.projects.api.layers.EnumAttributeType
import sestra.projects.api.layers.FloatAttributeType
import sestra.projects.api.layers.IntAttributeType
import sestra.projects.api.layers.Layer
import sestra.projects.api.layers.RelationLayerSpanRole
import sestra.projects.api.layers.RelationLayerType
import sestra.projects.api.layers.SpanLayerType
import sestra.projects.api.layers.StringAttributeType
import sestra.projects.api.projects.GetProjectsNamesResult
import sestra.projects.api.projects.InvalidProject
import sestra.projects.api.projects.Project
import sestra.projects.api.projects.ProjectAlreadyExists
import sestra.projects.api.projects.ProjectCreated
import sestra.projects.api.projects.ProjectsStore
import sestra.projects.rest.projects.serde.JacksonCustomizer

@WebMvcTest(controllers = [ProjectsStoreRestController::class])
@Import(WebSecurityConfig::class, JacksonCustomizer::class)
class ProjectsStoreRestControllerTest {
    @MockBean
    private lateinit var store: ProjectsStore

    @Autowired
    private lateinit var client: MockMvc

    private val project = Project(
        name = "project",
        layers = listOf(
            Layer(
                name = "rel",
                type = RelationLayerType(
                    spanRoles = listOf(
                        RelationLayerSpanRole(
                            name = "role1",
                            targetLayerName = "span"
                        ),
                        RelationLayerSpanRole(
                            name = "role2",
                            targetLayerName = "span"
                        )
                    )
                ),
                attrs = listOf(
                    Attribute(
                        name = "attr1",
                        type = BooleanAttributeType
                    ),
                    Attribute(
                        name = "attr2",
                        type = IntAttributeType
                    ),
                    Attribute(
                        name = "attr3",
                        type = FloatAttributeType
                    )
                )
            ),
            Layer(
                name = "span",
                type = SpanLayerType,
                attrs = listOf(
                    Attribute(
                        name = "attr1",
                        type = StringAttributeType
                    ),
                    Attribute(
                        name = "attr2",
                        type = EnumAttributeType(
                            values = listOf("value1", "value2", "value3")
                        )
                    ),
                    Attribute(
                        name = "attr3",
                        type = BooleanAttributeType
                    )
                )
            )
        )
    )

    private val projectJSON =
        """
            {
                "name": "project",
                "layers": [
                    {
                        "name": "rel",
                        "type": {
                            "name": "relation",
                            "spanRoles": [
                                {
                                    "name": "role1",
                                    "targetLayerName": "span"
                                },
                                {
                                    "name": "role2",
                                    "targetLayerName": "span"
                                }
                            ]
                        },
                        "attrs": [
                            {
                                "name": "attr1",
                                "type": {
                                    "name": "boolean"
                                }
                            },
                            {
                                "name": "attr2",
                                "type": {
                                    "name": "int"
                                }
                            },
                            {
                                "name": "attr3",
                                "type": {
                                    "name": "float"
                                }
                            }
                        ]
                    },
                    {
                        "name": "span",
                        "type": {
                            "name": "span"
                        },
                        "attrs": [
                            {
                                "name": "attr1",
                                "type": {
                                    "name": "string"
                                }
                            },
                            {
                                "name": "attr2",
                                "type": {
                                    "name": "enum",
                                    "values": [
                                        "value1",
                                        "value2",
                                        "value3"
                                    ]
                                }
                            },
                            {
                                "name": "attr3",
                                "type": {
                                    "name": "boolean"
                                }
                            }
                        ]
                    }
                ]
            }
        """.trimIndent()

    @Nested
    inner class Create {
        @Test
        fun `should return 401 for no creds`() {
            client.post("/api/projects") {
                accept = MediaType.APPLICATION_JSON
            }.andExpect {
                status { isUnauthorized() }
            }
        }

        @Test
        fun `should return 401 for incorrect creds`() {
            client.post("/api/projects") {
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
            client.post("/api/projects") {
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
        fun `should return 400 for invalid project`() {
            val project = Project(
                name = "project",
                layers = emptyList()
            )
            Mockito.`when`(store.createProject("admin", project))
                .thenReturn(
                    InvalidProject(
                        errors = listOf(
                            ValidationError("layers", "should not be empty")
                        )
                    )
                )

            client.post("/api/projects") {
                accept = MediaType.APPLICATION_JSON
                headers {
                    setBasicAuth("admin", "sestra")
                }
                contentType = MediaType.APPLICATION_JSON
                content = """
                    {
                        "name": "project",
                        "layers": []
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
                                        "field": "layers",
                                        "description": "should not be empty"
                                    }
                                ]
                            }
                        """.trimIndent()
                    )
                }
            }
        }

        @Test
        fun `should return 400 for project which already exists`() {
            Mockito.`when`(store.createProject("admin", project))
                .thenReturn(ProjectAlreadyExists)

            client.post("/api/projects") {
                accept = MediaType.APPLICATION_JSON
                headers {
                    setBasicAuth("admin", "sestra")
                }
                contentType = MediaType.APPLICATION_JSON
                content = projectJSON
            }.andExpect {
                status { isBadRequest() }
            }
        }

        @Test
        fun `should return 200 for correct project`() {
            Mockito.`when`(store.createProject("admin", project))
                .thenReturn(ProjectCreated)

            client.post("/api/projects") {
                accept = MediaType.APPLICATION_JSON
                headers {
                    setBasicAuth("admin", "sestra")
                }
                contentType = MediaType.APPLICATION_JSON
                content = projectJSON
            }.andExpect {
                status { isOk() }
            }
        }
    }

    @Nested
    inner class GetByName {
        @Test
        fun `should return 401 for no creds`() {
            client.get("/api/projects?name=project") {
                accept = MediaType.APPLICATION_JSON
            }.andExpect {
                status { isUnauthorized() }
            }
        }

        @Test
        fun `should return 401 for incorrect creds`() {
            client.get("/api/projects?name=project") {
                accept = MediaType.APPLICATION_JSON
                headers {
                    setBasicAuth("admin", "admin")
                }
            }.andExpect {
                status { isUnauthorized() }
            }
        }

        @Test
        fun `should return 400 when no name given`() {
            client.get("/api/projects") {
                accept = MediaType.APPLICATION_JSON
                headers {
                    setBasicAuth("admin", "sestra")
                }
            }.andExpect {
                status { isBadRequest() }
            }
        }

        @Test
        fun `should return 404 for unknown project`() {
            Mockito.`when`(store.getProjectByName("admin", "project"))
                .thenReturn(null)

            client.get("/api/projects?name=project") {
                accept = MediaType.APPLICATION_JSON
                headers {
                    setBasicAuth("admin", "sestra")
                }
            }.andExpect {
                status { isNotFound() }
            }
        }

        @Test
        fun `should return 200 and provide project with given name in JSON`() {
            Mockito.`when`(store.getProjectByName("admin", "project"))
                .thenReturn(project)

            client.get("/api/projects?name=project") {
                accept = MediaType.APPLICATION_JSON
                headers {
                    setBasicAuth("admin", "sestra")
                }
            }.andExpect {
                status { isOk() }
                content {
                    contentType(MediaType.APPLICATION_JSON)
                    json(projectJSON)
                }
            }
        }
    }

    @Nested
    inner class GetNames {
        @Test
        fun `should return 401 for no creds`() {
            client.get("/api/projects/names") {
                accept = MediaType.APPLICATION_JSON
            }.andExpect {
                status { isUnauthorized() }
            }
        }

        @Test
        fun `should return 401 for incorrect creds`() {
            client.get("/api/projects/names") {
                accept = MediaType.APPLICATION_JSON
                headers {
                    setBasicAuth("admin", "admin")
                }
            }.andExpect {
                status { isUnauthorized() }
            }
        }

        @Test
        fun `should return 200 and provide given projects names in JSON`() {
            Mockito.`when`(store.getProjectNames("admin"))
                .thenReturn(GetProjectsNamesResult(listOf("project1", "project2")))

            client.get("/api/projects/names") {
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
                                    "project1",
                                    "project2"
                                ]
                            }
                        """.trimIndent()
                    )
                }
            }
        }
    }
}
