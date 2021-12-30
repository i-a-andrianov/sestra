package sestra.projects.impl

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import sestra.common.api.ValidationError
import sestra.projects.api.layers.Attribute
import sestra.projects.api.layers.AttributeType
import sestra.projects.api.layers.Layer
import sestra.projects.api.layers.LayerType
import sestra.projects.api.layers.RelationLayerSpanRole
import sestra.projects.api.projects.CreateProjectResult
import sestra.projects.api.projects.GetProjectsNamesResult
import sestra.projects.api.projects.Project
import java.util.stream.Stream

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(ProjectsStoreImpl::class)
class ProjectsStoreImplProjectsTest {
    @Autowired
    private lateinit var store: ProjectsStoreImpl

    private val whoami = "admin"

    @Test
    fun `should return nothing on empty database`() {
        assertEquals(
            GetProjectsNamesResult(emptyList()),
            store.getProjectNames(whoami)
        )
        assertNull(
            store.getProjectByName(whoami, "project")
        )
    }

    @ParameterizedTest(name = "{displayName} {1}")
    @ArgumentsSource(CreateVisibleTestCases::class)
    @Suppress("UNUSED_PARAMETER")
    fun `created project should become visible with the same data`(project: Project, testCase: String) {
        val result = store.createProject(whoami, project)

        assertEquals(CreateProjectResult.ProjectCreated, result)
        assertEquals(
            GetProjectsNamesResult(listOf(project.name)),
            store.getProjectNames(whoami)
        )
        assertEquals(
            project,
            store.getProjectByName(whoami, project.name)
        )
    }

    private class CreateVisibleTestCases : ArgumentsProvider {
        override fun provideArguments(context: ExtensionContext?): Stream<out Arguments> {
            return Stream.of(
                Arguments.of(
                    Project(
                        "project 1",
                        layers = listOf(
                            Layer(
                                name = "layer 1",
                                type = LayerType.Span,
                                attrs = listOf(
                                    Attribute(
                                        name = "attr 1",
                                        type = AttributeType.Boolean
                                    ),
                                    Attribute(
                                        name = "attr 2",
                                        type = AttributeType.Int
                                    ),
                                    Attribute(
                                        name = "attr 3",
                                        type = AttributeType.Float
                                    )
                                )
                            ),
                            Layer(
                                name = "layer 2",
                                type = LayerType.Span,
                                attrs = listOf(
                                    Attribute(
                                        name = "attr 1",
                                        type = AttributeType.Int
                                    ),
                                    Attribute(
                                        name = "attr 2",
                                        type = AttributeType.String
                                    ),
                                    Attribute(
                                        name = "attr 3",
                                        type = AttributeType.Enum(
                                            values = listOf("value 1", "value 2", "value 3")
                                        )
                                    )
                                )
                            )
                        )
                    ),
                    "two span layers with attrs"
                ),
                Arguments.of(
                    Project(
                        "project 2",
                        layers = listOf(
                            Layer(
                                name = "layer 1",
                                type = LayerType.Span,
                                attrs = emptyList()
                            ),
                            Layer(
                                name = "layer 2",
                                type = LayerType.Relation(
                                    spanRoles = listOf(
                                        RelationLayerSpanRole(
                                            name = "role 1",
                                            targetLayerName = "layer 1"
                                        ),
                                        RelationLayerSpanRole(
                                            name = "role 2",
                                            targetLayerName = "layer 1"
                                        )
                                    )
                                ),
                                attrs = emptyList()
                            )
                        )
                    ),
                    "span and relation layers without attrs"
                )
            )
        }
    }

    @Test
    fun `create should return validation errors for invalid project`() {
        val project = Project(
            name = "  ",
            layers = emptyList()
        )
        val result = store.createProject(whoami, project)

        assertTrue(
            result is CreateProjectResult.InvalidProject
        )

        val errors = (result as CreateProjectResult.InvalidProject).errors

        assertEquals(
            setOf("name", "layers"),
            errors.map(ValidationError::field).toSet()
        )

        assertEquals(
            GetProjectsNamesResult(emptyList()),
            store.getProjectNames(whoami)
        )
        assertNull(
            store.getProjectByName(whoami, project.name)
        )
    }

    @Test
    fun `create should return already exists error for duplicated project name`() {
        val result1 = store.createProject(whoami, project1)

        assertEquals(CreateProjectResult.ProjectCreated, result1)
        assertEquals(
            GetProjectsNamesResult(listOf(project1.name)),
            store.getProjectNames(whoami)
        )
        assertEquals(
            project1,
            store.getProjectByName(whoami, project1.name)
        )

        val result2 = store.createProject(whoami, project2)

        assertEquals(CreateProjectResult.ProjectAlreadyExists, result2)
        assertEquals(
            GetProjectsNamesResult(listOf(project1.name)),
            store.getProjectNames(whoami)
        )
        assertEquals(
            project1,
            store.getProjectByName(whoami, project1.name)
        )
    }

    private val project1 = Project(
        name = "project",
        layers = listOf(
            Layer(
                name = "layer1",
                type = LayerType.Span,
                attrs = emptyList()
            )
        )
    )

    private val project2 = Project(
        name = project1.name,
        layers = listOf(
            Layer(
                name = "layer2",
                type = LayerType.Span,
                attrs = emptyList()
            )
        )
    )
}
