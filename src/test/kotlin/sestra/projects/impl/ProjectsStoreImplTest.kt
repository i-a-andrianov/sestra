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
import sestra.projects.api.core.Attribute
import sestra.projects.api.core.BooleanAttributeType
import sestra.projects.api.core.EnumAttributeType
import sestra.projects.api.core.FloatAttributeType
import sestra.projects.api.core.IntAttributeType
import sestra.projects.api.core.Layer
import sestra.projects.api.core.Project
import sestra.projects.api.core.RelationLayerSpanRole
import sestra.projects.api.core.RelationLayerType
import sestra.projects.api.core.SpanLayerType
import sestra.projects.api.core.StringAttributeType
import sestra.projects.api.store.CreateAlreadyExistsError
import sestra.projects.api.store.CreateInvalidProjectError
import sestra.projects.api.store.CreateSuccess
import java.util.stream.Stream

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(ProjectsStoreImpl::class)
class ProjectsStoreImplTest {
    @Autowired
    private lateinit var store: ProjectsStoreImpl

    @Test
    fun `should return nothing on empty database`() {
        assertEquals(
            emptyList<String>(),
            store.getNames("admin")
        )
        assertNull(
            store.getByName("admin", "project")
        )
    }

    @ParameterizedTest(name = "{displayName} {1}")
    @ArgumentsSource(CreateVisibleTestCases::class)
    @Suppress("UNUSED_PARAMETER")
    fun `created project should become visible with the same data`(project: Project, testCase: String) {
        val result = store.create("admin", project)

        assertTrue(
            result is CreateSuccess
        )
        assertEquals(
            listOf(project.name),
            store.getNames("admin")
        )
        assertEquals(
            project,
            store.getByName("admin", project.name)
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
                                type = SpanLayerType,
                                attrs = listOf(
                                    Attribute(
                                        name = "attr 1",
                                        type = BooleanAttributeType
                                    ),
                                    Attribute(
                                        name = "attr 2",
                                        type = IntAttributeType
                                    ),
                                    Attribute(
                                        name = "attr 3",
                                        type = FloatAttributeType
                                    )
                                )
                            ),
                            Layer(
                                name = "layer 2",
                                type = SpanLayerType,
                                attrs = listOf(
                                    Attribute(
                                        name = "attr 1",
                                        type = IntAttributeType
                                    ),
                                    Attribute(
                                        name = "attr 2",
                                        type = StringAttributeType
                                    ),
                                    Attribute(
                                        name = "attr 3",
                                        type = EnumAttributeType(
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
                                type = SpanLayerType,
                                attrs = emptyList()
                            ),
                            Layer(
                                name = "layer 2",
                                type = RelationLayerType(
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
        val result = store.create("admin", project)

        assertTrue(
            result is CreateInvalidProjectError
        )

        val errors = (result as CreateInvalidProjectError).errors

        assertEquals(
            setOf("name", "layers"),
            errors.map(ValidationError::field).toSet()
        )

        assertEquals(
            emptyList<String>(),
            store.getNames("admin")
        )
        assertNull(
            store.getByName("admin", project.name)
        )
    }

    @Test
    fun `create should return already exists error for duplicated project name`() {
        val project1 = Project(
            name = "project",
            layers = listOf(
                Layer(
                    name = "layer1",
                    type = SpanLayerType,
                    attrs = emptyList()
                )
            )
        )
        val result1 = store.create("admin", project1)

        assertTrue(
            result1 is CreateSuccess
        )
        assertEquals(
            listOf(project1.name),
            store.getNames("admin")
        )
        assertEquals(
            project1,
            store.getByName("admin", project1.name)
        )

        val project2 = Project(
            name = project1.name,
            layers = listOf(
                Layer(
                    name = "layer2",
                    type = SpanLayerType,
                    attrs = emptyList()
                )
            )
        )
        val result2 = store.create("admin", project2)

        assertTrue(
            result2 is CreateAlreadyExistsError
        )
        assertEquals(
            listOf(project1.name),
            store.getNames("admin")
        )
        assertEquals(
            project1,
            store.getByName("admin", project1.name)
        )
    }
}
