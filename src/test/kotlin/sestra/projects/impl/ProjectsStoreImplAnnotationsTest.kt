package sestra.projects.impl

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import sestra.projects.api.annotations.Annotation
import sestra.projects.api.annotations.AnnotationAttribute
import sestra.projects.api.annotations.AnnotationContainer
import sestra.projects.api.annotations.AnnotationValue
import sestra.projects.api.annotations.AttributeValue
import sestra.projects.api.annotations.CreateAnnotationResult
import sestra.projects.api.annotations.DeleteAnnotationResult
import sestra.projects.api.annotations.RelationAnnotationSpanRole
import sestra.projects.api.documents.Document
import sestra.projects.api.documents.DocumentContainer
import sestra.projects.api.layers.Attribute
import sestra.projects.api.layers.AttributeType
import sestra.projects.api.layers.Layer
import sestra.projects.api.layers.LayerType
import sestra.projects.api.layers.RelationLayerSpanRole
import sestra.projects.api.projects.Project
import java.util.UUID

@DataJpaTest(properties = [TestContainersPostgres.url])
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(ProjectsStoreImpl::class)
class ProjectsStoreImplAnnotationsTest {
    @Autowired
    private lateinit var store: ProjectsStoreImpl

    private val user1 = "user1"
    private val user2 = "user2"

    private val project = Project(
        name = "project1",
        layers = listOf(
            Layer(
                name = "layer1",
                type = LayerType.Span,
                attrs = listOf(
                    Attribute(
                        name = "attr1",
                        type = AttributeType.Boolean
                    ),
                    Attribute(
                        name = "attr2",
                        type = AttributeType.Int
                    ),
                    Attribute(
                        name = "attr3",
                        type = AttributeType.Float
                    )
                )
            ),
            Layer(
                name = "layer2",
                type = LayerType.Relation(
                    spanRoles = listOf(
                        RelationLayerSpanRole(
                            name = "role1",
                            targetLayerName = "layer1"
                        ),
                        RelationLayerSpanRole(
                            name = "role2",
                            targetLayerName = "layer1"
                        )
                    )
                ),
                attrs = listOf(
                    Attribute(
                        name = "attr1",
                        type = AttributeType.String
                    ),
                    Attribute(
                        name = "attr2",
                        type = AttributeType.Enum(
                            values = listOf("value1", "value2")
                        )
                    )
                )
            )
        )
    )

    private val documentContainer = DocumentContainer(
        projectName = project.name
    )

    private val document = Document(
        name = "doc1",
        text = "Hello, world!"
    )

    private val annotationContainer1 = AnnotationContainer(
        projectName = project.name,
        documentName = document.name,
        layerName = project.layers[0].name
    )

    private val annotation1 = Annotation(
        id = UUID.randomUUID(),
        value = AnnotationValue.Span(
            start = 5,
            end = 6
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

    private val annotationContainer2 = AnnotationContainer(
        projectName = project.name,
        documentName = document.name,
        layerName = project.layers[1].name
    )

    private val annotation2 = Annotation(
        id = UUID.randomUUID(),
        value = AnnotationValue.Relation(
            spanRoles = listOf(
                RelationAnnotationSpanRole(
                    name = "role1",
                    targetAnnotationId = annotation1.id
                ),
                RelationAnnotationSpanRole(
                    name = "role2",
                    targetAnnotationId = annotation1.id
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

    @Test
    fun `should return nothing when no annotations saved`() {
        store.createProject(user1, project)
        store.createDocument(user1, documentContainer, document)

        assertEquals(
            emptyList<Annotation>(),
            store.getAnnotations(user1, annotationContainer1)
        )
    }

    @Test
    fun `create should return project not found when so`() {
        assertEquals(
            CreateAnnotationResult.ProjectNotFound,
            store.createAnnotation(user1, annotationContainer1, annotation1)
        )
        assertEquals(
            emptyList<Annotation>(),
            store.getAnnotations(user1, annotationContainer1)
        )
    }

    @Test
    fun `create should return document not found when so`() {
        store.createProject(user1, project)

        assertEquals(
            CreateAnnotationResult.DocumentNotFound,
            store.createAnnotation(user1, annotationContainer1, annotation1)
        )
        assertEquals(
            emptyList<Annotation>(),
            store.getAnnotations(user1, annotationContainer1)
        )
    }

    @Test
    fun `create should return layer not found when so`() {
        store.createProject(user1, project)
        store.createDocument(user1, documentContainer, document)

        val container = annotationContainer1.copy(layerName = "layer3")

        assertEquals(
            CreateAnnotationResult.LayerNotFound,
            store.createAnnotation(user1, container, annotation1)
        )
        assertEquals(
            emptyList<Annotation>(),
            store.getAnnotations(user1, container)
        )
    }

    @Test
    fun `create should return invalid annotation if so`() {
        store.createProject(user1, project)
        store.createDocument(user1, documentContainer, document)

        val annotation = annotation1.copy(attrs = emptyList())

        assertInstanceOf(
            CreateAnnotationResult.InvalidAnnotation::class.java,
            store.createAnnotation(user1, annotationContainer1, annotation)
        )
        assertEquals(
            emptyList<Annotation>(),
            store.getAnnotations(user1, annotationContainer1)
        )
    }

    @Test
    fun `second create of the same annotation should return already exists`() {
        store.createProject(user1, project)
        store.createDocument(user1, documentContainer, document)
        store.createAnnotation(user1, annotationContainer1, annotation1)

        assertEquals(
            CreateAnnotationResult.AnnotationAlreadyExists,
            store.createAnnotation(user1, annotationContainer1, annotation1)
        )
    }

    @Test
    fun `created annotations should become visible for the same user`() {
        store.createProject(user1, project)
        store.createDocument(user1, documentContainer, document)

        assertEquals(
            CreateAnnotationResult.AnnotationCreated,
            store.createAnnotation(user1, annotationContainer1, annotation1)
        )
        assertEquals(
            listOf(annotation1),
            store.getAnnotations(user1, annotationContainer1)
        )
        assertEquals(
            emptyList<Annotation>(),
            store.getAnnotations(user1, annotationContainer2)
        )

        assertEquals(
            CreateAnnotationResult.AnnotationCreated,
            store.createAnnotation(user1, annotationContainer2, annotation2)
        )
        assertEquals(
            listOf(annotation1),
            store.getAnnotations(user1, annotationContainer1)
        )
        assertEquals(
            listOf(annotation2),
            store.getAnnotations(user1, annotationContainer2)
        )
    }

    @Test
    fun `created annotation should not become visible for another user`() {
        store.createProject(user1, project)
        store.createDocument(user1, documentContainer, document)

        assertEquals(
            CreateAnnotationResult.AnnotationCreated,
            store.createAnnotation(user1, annotationContainer1, annotation1)
        )
        assertEquals(
            emptyList<Annotation>(),
            store.getAnnotations(user2, annotationContainer1)
        )
    }

    @Test
    fun `delete should return annotation not found when so`() {
        store.createProject(user1, project)
        store.createDocument(user1, documentContainer, document)

        assertEquals(
            DeleteAnnotationResult.AnnotationNotFound,
            store.deleteAnnotation(user1, annotation1.id)
        )
    }

    @Test
    fun `delete should return annotation not found when it is created by another user`() {
        store.createProject(user1, project)
        store.createDocument(user1, documentContainer, document)
        store.createAnnotation(user1, annotationContainer1, annotation1)

        assertEquals(
            DeleteAnnotationResult.AnnotationNotFound,
            store.deleteAnnotation(user2, annotation1.id)
        )
    }

    @Test
    fun `delete should detect that annotation is referenced by others`() {
        store.createProject(user1, project)
        store.createDocument(user1, documentContainer, document)
        store.createAnnotation(user1, annotationContainer1, annotation1)
        store.createAnnotation(user1, annotationContainer2, annotation2)

        assertEquals(
            DeleteAnnotationResult.AnnotationIsReferencedByOthers(
                ids = listOf(annotation2.id)
            ),
            store.deleteAnnotation(user1, annotation1.id)
        )
    }

    @Test
    fun `delete should delete annotations`() {
        store.createProject(user1, project)
        store.createDocument(user1, documentContainer, document)
        store.createAnnotation(user1, annotationContainer1, annotation1)
        store.createAnnotation(user1, annotationContainer2, annotation2)

        assertEquals(
            DeleteAnnotationResult.AnnotationDeleted,
            store.deleteAnnotation(user1, annotation2.id)
        )
        assertEquals(
            listOf(annotation1),
            store.getAnnotations(user1, annotationContainer1)
        )
        assertEquals(
            emptyList<Annotation>(),
            store.getAnnotations(user1, annotationContainer2)
        )

        assertEquals(
            DeleteAnnotationResult.AnnotationDeleted,
            store.deleteAnnotation(user1, annotation1.id)
        )
        assertEquals(
            emptyList<Annotation>(),
            store.getAnnotations(user1, annotationContainer1)
        )
        assertEquals(
            emptyList<Annotation>(),
            store.getAnnotations(user1, annotationContainer2)
        )
    }
}
