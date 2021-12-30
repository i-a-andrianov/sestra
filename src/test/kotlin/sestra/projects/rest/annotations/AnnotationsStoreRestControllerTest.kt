package sestra.projects.rest.annotations

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import sestra.common.api.ValidationError
import sestra.common.rest.WebSecurityConfig
import sestra.projects.api.annotations.Annotation
import sestra.projects.api.annotations.AnnotationAttribute
import sestra.projects.api.annotations.AnnotationContainer
import sestra.projects.api.annotations.AnnotationValue
import sestra.projects.api.annotations.AnnotationsStore
import sestra.projects.api.annotations.AttributeValue
import sestra.projects.api.annotations.CreateAnnotationResult
import sestra.projects.api.annotations.DeleteAnnotationResult
import sestra.projects.api.annotations.RelationAnnotationSpanRole
import sestra.projects.rest.annotations.serde.AnnotationsStoreJacksonCustomizer
import java.util.UUID
import java.util.stream.Stream

@WebMvcTest(controllers = [AnnotationsStoreRestController::class])
@Import(WebSecurityConfig::class, AnnotationsStoreJacksonCustomizer::class)
class AnnotationsStoreRestControllerTest {
    @MockBean
    private lateinit var store: AnnotationsStore

    @Autowired
    private lateinit var client: MockMvc

    private object Data {
        val uuid1 = UUID.randomUUID()

        val annotation1 = Annotation(
            id = uuid1,
            value = AnnotationValue.Span(
                start = 10,
                end = 20
            ),
            attrs = listOf(
                AnnotationAttribute(
                    name = "attr1",
                    value = AttributeValue.Boolean(value = true)
                ),
                AnnotationAttribute(
                    name = "attr2",
                    value = AttributeValue.Int(value = 10)
                ),
                AnnotationAttribute(
                    name = "attr3",
                    value = AttributeValue.Float(value = 3.14f)
                )
            )
        )

        val annotation1JSON =
            """
                {
                    "id": "$uuid1",
                    "value": {
                        "type": "span",
                        "start": 10,
                        "end": 20
                    },
                    "attrs": [
                        {
                            "name": "attr1",
                            "value": {
                                "type": "boolean",
                                "value": true
                            }
                        },
                        {
                            "name": "attr2",
                            "value": {
                                "type": "int",
                                "value": 10
                            }
                        },
                        {
                            "name": "attr3",
                            "value": {
                                "type": "float",
                                "value": 3.14
                            }
                        }
                    ]
                }
            """.trimIndent()

        val uuid2 = UUID.randomUUID()

        val annotation2 = Annotation(
            id = uuid2,
            value = AnnotationValue.Relation(
                spanRoles = listOf(
                    RelationAnnotationSpanRole(
                        name = "role1",
                        targetAnnotationId = uuid1
                    ),
                    RelationAnnotationSpanRole(
                        name = "role2",
                        targetAnnotationId = uuid1
                    )
                )
            ),
            attrs = listOf(
                AnnotationAttribute(
                    name = "attr1",
                    value = AttributeValue.String(value = "haha")
                ),
                AnnotationAttribute(
                    name = "attr2",
                    value = AttributeValue.Enum(value = "value1")
                )
            )
        )

        val annotation2JSON =
            """
                {
                    "id": "$uuid2",
                    "value": {
                        "type": "relation",
                        "spanRoles": [
                            {
                                "name": "role1",
                                "targetAnnotationId": "$uuid1"
                            },
                            {
                                "name": "role2",
                                "targetAnnotationId": "$uuid1"
                            }
                        ]
                    },
                    "attrs": [
                        {
                            "name": "attr1",
                            "value": {
                                "type": "string",
                                "value": "haha"
                            }
                        },
                        {
                            "name": "attr2",
                            "value": {
                                "type": "enum",
                                "value": "value1"
                            }
                        }
                    ]
                }
            """.trimIndent()
    }

    @Nested
    inner class Create {
        @Test
        fun `should return 401 for no creds`() {
            client.post("/api/projects/annotations") {
                accept = MediaType.APPLICATION_JSON
            }.andExpect {
                status { isUnauthorized() }
            }
        }

        @Test
        fun `should return 401 for incorrect creds`() {
            client.post("/api/projects/annotations") {
                accept = MediaType.APPLICATION_JSON
                headers {
                    setBasicAuth("admin", "admin")
                }
            }.andExpect {
                status { isUnauthorized() }
            }
        }

        @Test
        fun `should return 400 when project name not given`() {
            client.post("/api/projects/annotations?documentName=d1&layerName=l1") {
                accept = MediaType.APPLICATION_JSON
                headers {
                    setBasicAuth("admin", "sestra")
                }
                contentType = MediaType.APPLICATION_JSON
                content = Data.annotation1JSON
            }.andExpect {
                status { isBadRequest() }
            }
        }

        @Test
        fun `should return 400 when document name not given`() {
            client.post("/api/projects/annotations?projectName=p1&layerName=l1") {
                accept = MediaType.APPLICATION_JSON
                headers {
                    setBasicAuth("admin", "sestra")
                }
                contentType = MediaType.APPLICATION_JSON
                content = Data.annotation1JSON
            }.andExpect {
                status { isBadRequest() }
            }
        }

        @Test
        fun `should return 400 when layer name not given`() {
            client.post("/api/projects/annotations?projectName=p1&documentName=d1") {
                accept = MediaType.APPLICATION_JSON
                headers {
                    setBasicAuth("admin", "sestra")
                }
                contentType = MediaType.APPLICATION_JSON
                content = Data.annotation1JSON
            }.andExpect {
                status { isBadRequest() }
            }
        }

        @Test
        fun `should return 400 when incorrect payload given`() {
            client.post("/api/projects/annotations?projectName=p1&documentName=d1&layerName=l1") {
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
        fun `should return 400 when invalid annotation given`() {
            val container = AnnotationContainer("p1", "d1", "l1")
            Mockito.`when`(store.createAnnotation("admin", container, Data.annotation1))
                .thenReturn(
                    CreateAnnotationResult.InvalidAnnotation(
                        errors = listOf(
                            ValidationError("value", "is span while layer is relation")
                        )
                    )
                )

            client.post("/api/projects/annotations?projectName=p1&documentName=d1&layerName=l1") {
                accept = MediaType.APPLICATION_JSON
                headers {
                    setBasicAuth("admin", "sestra")
                }
                contentType = MediaType.APPLICATION_JSON
                content = Data.annotation1JSON
            }.andExpect {
                status { isBadRequest() }
                content {
                    contentType(MediaType.APPLICATION_JSON)
                    json(
                        """
                            {
                                "errors": [
                                    {"field": "value", "description": "is span while layer is relation"}
                                ]
                            }
                        """.trimIndent()
                    )
                }
            }
        }

        @Test
        fun `should return 400 when project not found`() {
            val container = AnnotationContainer("p1", "d1", "l1")
            Mockito.`when`(store.createAnnotation("admin", container, Data.annotation1))
                .thenReturn(CreateAnnotationResult.ProjectNotFound)

            client.post("/api/projects/annotations?projectName=p1&documentName=d1&layerName=l1") {
                accept = MediaType.APPLICATION_JSON
                headers {
                    setBasicAuth("admin", "sestra")
                }
                contentType = MediaType.APPLICATION_JSON
                content = Data.annotation1JSON
            }.andExpect {
                status { isBadRequest() }
            }
        }

        @Test
        fun `should return 400 when document not found`() {
            val container = AnnotationContainer("p1", "d1", "l1")
            Mockito.`when`(store.createAnnotation("admin", container, Data.annotation1))
                .thenReturn(CreateAnnotationResult.DocumentNotFound)

            client.post("/api/projects/annotations?projectName=p1&documentName=d1&layerName=l1") {
                accept = MediaType.APPLICATION_JSON
                headers {
                    setBasicAuth("admin", "sestra")
                }
                contentType = MediaType.APPLICATION_JSON
                content = Data.annotation1JSON
            }.andExpect {
                status { isBadRequest() }
            }
        }

        @Test
        fun `should return 400 when layer not found`() {
            val container = AnnotationContainer("p1", "d1", "l1")
            Mockito.`when`(store.createAnnotation("admin", container, Data.annotation1))
                .thenReturn(CreateAnnotationResult.LayerNotFound)

            client.post("/api/projects/annotations?projectName=p1&documentName=d1&layerName=l1") {
                accept = MediaType.APPLICATION_JSON
                headers {
                    setBasicAuth("admin", "sestra")
                }
                contentType = MediaType.APPLICATION_JSON
                content = Data.annotation1JSON
            }.andExpect {
                status { isBadRequest() }
            }
        }

        @Test
        fun `should return 400 when annotation already exists`() {
            val container = AnnotationContainer("p1", "d1", "l1")
            Mockito.`when`(store.createAnnotation("admin", container, Data.annotation1))
                .thenReturn(CreateAnnotationResult.AnnotationAlreadyExists)

            client.post("/api/projects/annotations?projectName=p1&documentName=d1&layerName=l1") {
                accept = MediaType.APPLICATION_JSON
                headers {
                    setBasicAuth("admin", "sestra")
                }
                contentType = MediaType.APPLICATION_JSON
                content = Data.annotation1JSON
            }.andExpect {
                status { isBadRequest() }
            }
        }

        @ParameterizedTest(name = "{displayName} {2}")
        @ArgumentsSource(CreateTestCases::class)
        @Suppress("UNUSED_PARAMETER")
        fun `should return 200 when correct annotation given`(
            annotation: Annotation,
            annotationJSON: String,
            testCase: String
        ) {
            val container = AnnotationContainer("p1", "d1", "l1")
            Mockito.`when`(store.createAnnotation("admin", container, annotation))
                .thenReturn(CreateAnnotationResult.AnnotationCreated)

            client.post("/api/projects/annotations?projectName=p1&documentName=d1&layerName=l1") {
                accept = MediaType.APPLICATION_JSON
                headers {
                    setBasicAuth("admin", "sestra")
                }
                contentType = MediaType.APPLICATION_JSON
                content = annotationJSON
            }.andExpect {
                status { isOk() }
            }
        }
    }

    private class CreateTestCases : ArgumentsProvider {
        override fun provideArguments(context: ExtensionContext?): Stream<out Arguments> {
            return Stream.of(
                Arguments.of(Data.annotation1, Data.annotation1JSON, "span"),
                Arguments.of(Data.annotation2, Data.annotation2JSON, "relation")
            )
        }
    }

    @Nested
    inner class Get {
        @Test
        fun `should return 401 for no creds`() {
            client.get("/api/projects/annotations") {
                accept = MediaType.APPLICATION_JSON
            }.andExpect {
                status { isUnauthorized() }
            }
        }

        @Test
        fun `should return 401 for incorrect creds`() {
            client.get("/api/projects/annotations") {
                accept = MediaType.APPLICATION_JSON
                headers {
                    setBasicAuth("admin", "admin")
                }
            }.andExpect {
                status { isUnauthorized() }
            }
        }

        @Test
        fun `should return 400 when project name not given`() {
            client.get("/api/projects/annotations?documentName=d1&layerName=l1") {
                accept = MediaType.APPLICATION_JSON
                headers {
                    setBasicAuth("admin", "sestra")
                }
            }.andExpect {
                status { isBadRequest() }
            }
        }

        @Test
        fun `should return 400 when document name not given`() {
            client.get("/api/projects/annotations?projectName=p1&layerName=l1") {
                accept = MediaType.APPLICATION_JSON
                headers {
                    setBasicAuth("admin", "sestra")
                }
            }.andExpect {
                status { isBadRequest() }
            }
        }

        @Test
        fun `should return 400 when layer name not given`() {
            client.get("/api/projects/annotations?projectName=p1&documentName=d1") {
                accept = MediaType.APPLICATION_JSON
                headers {
                    setBasicAuth("admin", "sestra")
                }
            }.andExpect {
                status { isBadRequest() }
            }
        }

        @ParameterizedTest(name = "{displayName} {2}")
        @ArgumentsSource(GetTestCases::class)
        @Suppress("UNUSED_PARAMETER")
        fun `should return 200 with annotations in body if correct`(
            annotation: Annotation,
            annotationJSON: String,
            testCase: String
        ) {
            val container = AnnotationContainer("p1", "d1", "l1")
            Mockito.`when`(store.getAnnotations("admin", container))
                .thenReturn(listOf(annotation))

            client.get("/api/projects/annotations?projectName=p1&documentName=d1&layerName=l1") {
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
                            [ $annotationJSON ]
                        """.trimIndent()
                    )
                }
            }
        }
    }

    private class GetTestCases : ArgumentsProvider {
        override fun provideArguments(context: ExtensionContext?): Stream<out Arguments> {
            return Stream.of(
                Arguments.of(Data.annotation1, Data.annotation1JSON, "span"),
                Arguments.of(Data.annotation2, Data.annotation2JSON, "relation")
            )
        }
    }

    @Nested
    inner class Delete {
        @Test
        fun `should return 401 for no creds`() {
            client.delete("/api/projects/annotations/${Data.uuid1}") {
                accept = MediaType.APPLICATION_JSON
            }.andExpect {
                status { isUnauthorized() }
            }
        }

        @Test
        fun `should return 401 for incorrect creds`() {
            client.delete("/api/projects/annotations/${Data.uuid1}") {
                accept = MediaType.APPLICATION_JSON
                headers {
                    setBasicAuth("admin", "admin")
                }
            }.andExpect {
                status { isUnauthorized() }
            }
        }

        @Test
        fun `should return 400 when id is not uuid`() {
            client.delete("/api/projects/annotations/10") {
                accept = MediaType.APPLICATION_JSON
                headers {
                    setBasicAuth("admin", "sestra")
                }
            }.andExpect {
                status { isBadRequest() }
            }
        }

        @Test
        fun `should return 400 when annotation is referenced by others`() {
            Mockito.`when`(store.deleteAnnotation("admin", Data.uuid1))
                .thenReturn(
                    DeleteAnnotationResult.AnnotationIsReferencedByOthers(
                        ids = listOf(Data.uuid2)
                    )
                )

            client.delete("/api/projects/annotations/${Data.uuid1}") {
                accept = MediaType.APPLICATION_JSON
                headers {
                    setBasicAuth("admin", "sestra")
                }
            }.andExpect {
                status { isBadRequest() }
                content {
                    contentType(MediaType.APPLICATION_JSON)
                    json(
                        """
                            {
                                "ids": [ "${Data.uuid2}" ]
                            }
                        """.trimIndent()
                    )
                }
            }
        }

        @Test
        fun `should return 404 when annotation is not found`() {
            Mockito.`when`(store.deleteAnnotation("admin", Data.uuid1))
                .thenReturn(DeleteAnnotationResult.AnnotationNotFound)

            client.delete("/api/projects/annotations/${Data.uuid1}") {
                accept = MediaType.APPLICATION_JSON
                headers {
                    setBasicAuth("admin", "sestra")
                }
            }.andExpect {
                status { isNotFound() }
            }
        }

        @Test
        fun `should return 200 when annotation deleted`() {
            Mockito.`when`(store.deleteAnnotation("admin", Data.uuid1))
                .thenReturn(DeleteAnnotationResult.AnnotationDeleted)

            client.delete("/api/projects/annotations/${Data.uuid1}") {
                accept = MediaType.APPLICATION_JSON
                headers {
                    setBasicAuth("admin", "sestra")
                }
            }.andExpect {
                status { isOk() }
            }
        }
    }
}
