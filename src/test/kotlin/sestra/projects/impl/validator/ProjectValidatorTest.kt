package sestra.projects.impl.validator

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import sestra.common.api.ValidationError
import sestra.projects.api.core.Attribute
import sestra.projects.api.core.BooleanAttributeType
import sestra.projects.api.core.EnumAttributeType
import sestra.projects.api.core.IntAttributeType
import sestra.projects.api.core.Layer
import sestra.projects.api.core.Project
import sestra.projects.api.core.RelationLayerSpanRole
import sestra.projects.api.core.RelationLayerType
import sestra.projects.api.core.SpanLayerType
import java.util.stream.Stream

class ProjectValidatorTest {
    private val validator = ProjectValidator()

    @ParameterizedTest(name = "{displayName} {2}")
    @ArgumentsSource(TestCases::class)
    @Suppress("UNUSED_PARAMETER")
    fun `validator should`(project: Project, expectedFields: Set<String>, testCase: String) {
        val actualErrors = validator.validate(project)
        val actualFields = actualErrors.map(ValidationError::field).toSet()
        assertEquals(expectedFields, actualFields)
    }

    private class TestCases : ArgumentsProvider {
        override fun provideArguments(context: ExtensionContext?): Stream<out Arguments> {
            return Stream.of(
                Arguments.of(
                    Project(
                        name = "project",
                        layers = emptyList()
                    ),
                    setOf("layers"),
                    "reject empty layers"
                ),

                Arguments.of(
                    Project(
                        name = "project",
                        layers = listOf(
                            Layer(
                                name = "layer",
                                type = RelationLayerType(
                                    spanRoles = listOf(
                                        RelationLayerSpanRole(
                                            name = "role",
                                            targetLayerName = "layer2"
                                        )
                                    )
                                ),
                                attrs = emptyList()
                            ),
                            Layer(
                                name = "layer2",
                                type = SpanLayerType,
                                attrs = emptyList()
                            )
                        )
                    ),
                    setOf("layers[0].type.spanRoles"),
                    "reject less than 2 span roles in relation layer"
                ),

                Arguments.of(
                    Project(
                        name = "project",
                        layers = listOf(
                            Layer(
                                name = "layer",
                                type = SpanLayerType,
                                attrs = listOf(
                                    Attribute(
                                        name = "attr",
                                        type = EnumAttributeType(
                                            values = listOf("value")
                                        )
                                    )
                                )
                            )
                        )
                    ),
                    setOf("layers[0].attrs[0].type.values"),
                    "reject less than 2 values in enum attribute"
                ),

                Arguments.of(
                    Project(
                        name = "project",
                        layers = listOf(
                            Layer(
                                name = "layer",
                                type = SpanLayerType,
                                attrs = listOf(
                                    Attribute(
                                        name = "attr",
                                        type = BooleanAttributeType
                                    )
                                )
                            ),
                            Layer(
                                name = "layer",
                                type = SpanLayerType,
                                attrs = emptyList()
                            )
                        )
                    ),
                    setOf("layers"),
                    "reject duplicated layer names"
                ),

                Arguments.of(
                    Project(
                        name = "project",
                        layers = listOf(
                            Layer(
                                name = "layer1",
                                type = RelationLayerType(
                                    spanRoles = listOf(
                                        RelationLayerSpanRole(
                                            name = "role1",
                                            targetLayerName = "layer2"
                                        ),
                                        RelationLayerSpanRole(
                                            name = "role2",
                                            targetLayerName = "layer2"
                                        ),
                                        RelationLayerSpanRole(
                                            name = "role1",
                                            targetLayerName = "layer2"
                                        )
                                    )
                                ),
                                attrs = emptyList()
                            ),
                            Layer(
                                name = "layer2",
                                type = SpanLayerType,
                                attrs = emptyList()
                            )
                        )
                    ),
                    setOf("layers[0].type.spanRoles"),
                    "reject duplicated roles in relation layer"
                ),

                Arguments.of(
                    Project(
                        name = "project",
                        layers = listOf(
                            Layer(
                                name = "layer",
                                type = SpanLayerType,
                                attrs = listOf(
                                    Attribute(
                                        name = "attr",
                                        type = BooleanAttributeType
                                    ),
                                    Attribute(
                                        name = "attr",
                                        type = IntAttributeType
                                    )
                                )
                            )
                        )
                    ),
                    setOf("layers[0].attrs"),
                    "reject duplicated attribute names in layer"
                ),

                Arguments.of(
                    Project(
                        name = "project",
                        layers = listOf(
                            Layer(
                                name = "layer",
                                type = SpanLayerType,
                                attrs = listOf(
                                    Attribute(
                                        name = "attr",
                                        type = EnumAttributeType(
                                            values = listOf("value1", "value1", "value2")
                                        )
                                    )
                                )
                            )
                        )
                    ),
                    setOf("layers[0].attrs[0].type.values"),
                    "reject duplicated values in attribute"
                ),

                Arguments.of(
                    Project(
                        name = "project",
                        layers = listOf(
                            Layer(
                                name = "layer",
                                type = RelationLayerType(
                                    spanRoles = listOf(
                                        RelationLayerSpanRole(
                                            name = "role1",
                                            targetLayerName = "layer2"
                                        ),
                                        RelationLayerSpanRole(
                                            name = "role2",
                                            targetLayerName = "layer"
                                        )
                                    )
                                ),
                                attrs = emptyList()
                            )
                        )
                    ),
                    setOf(
                        "layers[0].type.spanRoles[0].targetLayerName",
                        "layers[0].type.spanRoles[1].targetLayerName"
                    ),
                    "reject roles with non-existent or non-span target layers"
                ),

                Arguments.of(
                    Project(
                        name = "  ",
                        layers = listOf(
                            Layer(
                                name = "",
                                type = RelationLayerType(
                                    spanRoles = listOf(
                                        RelationLayerSpanRole(
                                            name = "role",
                                            targetLayerName = "layer"
                                        ),
                                        RelationLayerSpanRole(
                                            name = "",
                                            targetLayerName = "layer"
                                        )
                                    )
                                ),
                                attrs = listOf(
                                    Attribute(
                                        name = "    ",
                                        type = EnumAttributeType(
                                            values = listOf(" ", "value")
                                        )
                                    )
                                )
                            ),
                            Layer(
                                name = "layer",
                                type = SpanLayerType,
                                attrs = emptyList()
                            )
                        )
                    ),
                    setOf(
                        "name",
                        "layers[0].name",
                        "layers[0].type.spanRoles[1].name",
                        "layers[0].attrs[0].name",
                        "layers[0].attrs[0].type.values[0]"
                    ),
                    "reject blank names everywhere"
                ),

                Arguments.of(
                    Project(
                        name = "project",
                        layers = listOf(
                            Layer(
                                name = "layer1",
                                type = RelationLayerType(
                                    spanRoles = listOf(
                                        RelationLayerSpanRole(
                                            name = "role1",
                                            targetLayerName = "layer2"
                                        ),
                                        RelationLayerSpanRole(
                                            name = "role2",
                                            targetLayerName = "layer2"
                                        )
                                    )
                                ),
                                attrs = listOf(
                                    Attribute(
                                        name = "attr",
                                        type = EnumAttributeType(
                                            values = listOf("value1", "value2")
                                        )
                                    )
                                )
                            ),
                            Layer(
                                name = "layer2",
                                type = SpanLayerType,
                                attrs = emptyList()
                            )
                        )
                    ),
                    emptySet<String>(),
                    "accept correct projects"
                )
            )
        }
    }
}
