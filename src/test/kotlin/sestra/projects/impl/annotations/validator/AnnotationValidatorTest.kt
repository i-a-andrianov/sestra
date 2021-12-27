package sestra.projects.impl.annotations.validator

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import sestra.common.api.ValidationError
import sestra.projects.api.annotations.Annotation
import sestra.projects.api.annotations.AnnotationAttribute
import sestra.projects.api.annotations.BooleanAttributeValue
import sestra.projects.api.annotations.EnumAttributeValue
import sestra.projects.api.annotations.FloatAttributeValue
import sestra.projects.api.annotations.IntAttributeValue
import sestra.projects.api.annotations.RelationAnnotationSpanRole
import sestra.projects.api.annotations.RelationAnnotationValue
import sestra.projects.api.annotations.SpanAnnotationValue
import sestra.projects.api.annotations.StringAttributeValue
import sestra.projects.api.documents.Document
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
import java.util.UUID
import java.util.stream.Stream

class AnnotationValidatorTest {
    private val validator = AnnotationValidator()

    private val document = Document(
        name = "doc1",
        text = "Hello, world!"
    )

    @ParameterizedTest(name = "{displayName} {4}")
    @ArgumentsSource(TestCases::class)
    @Suppress("UNUSED_PARAMETER")
    fun `validator should`(
        annotation: Annotation,
        targetLayer: Layer,
        existsAnnotationByLayerNameAndId: (String, UUID) -> Boolean,
        expectedFields: Set<String>,
        testCase: String
    ) {
        val actualErrors = validator.validate(
            annotation,
            targetLayer,
            document,
            existsAnnotationByLayerNameAndId
        )
        val actualFields = actualErrors.map(ValidationError::field).toSet()
        assertEquals(expectedFields, actualFields)
    }

    private class TestCases : ArgumentsProvider {
        private val uuid1 = UUID.randomUUID()
        private val uuid2 = UUID.randomUUID()

        override fun provideArguments(context: ExtensionContext?): Stream<out Arguments> {
            return Stream.of(
                Arguments.of(
                    Annotation(
                        id = uuid1,
                        value = SpanAnnotationValue(
                            start = -1,
                            end = 5
                        ),
                        attrs = emptyList()
                    ),
                    Layer(
                        name = "layer1",
                        type = SpanLayerType,
                        attrs = emptyList()
                    ),
                    { _: String, _: UUID -> false },
                    setOf("value.start"),
                    "reject span with start outside of doc text"
                ),

                Arguments.of(
                    Annotation(
                        id = uuid1,
                        value = SpanAnnotationValue(
                            start = 5,
                            end = 5
                        ),
                        attrs = emptyList()
                    ),
                    Layer(
                        name = "layer1",
                        type = SpanLayerType,
                        attrs = emptyList()
                    ),
                    { _: String, _: UUID -> false },
                    setOf("value.end"),
                    "reject span with start not less than end"
                ),

                Arguments.of(
                    Annotation(
                        id = uuid1,
                        value = SpanAnnotationValue(
                            start = 5,
                            end = 14
                        ),
                        attrs = emptyList()
                    ),
                    Layer(
                        name = "layer1",
                        type = SpanLayerType,
                        attrs = emptyList()
                    ),
                    { _: String, _: UUID -> false },
                    setOf("value.end"),
                    "reject span with end outside of doc text"
                ),

                Arguments.of(
                    Annotation(
                        id = uuid1,
                        value = SpanAnnotationValue(
                            start = 5,
                            end = 6
                        ),
                        attrs = listOf(
                            AnnotationAttribute(
                                name = "attr1",
                                value = BooleanAttributeValue(value = true)
                            ),
                            AnnotationAttribute(
                                name = "attr1",
                                value = BooleanAttributeValue(value = false)
                            )
                        )
                    ),
                    Layer(
                        name = "layer1",
                        type = SpanLayerType,
                        attrs = listOf(
                            Attribute(
                                name = "attr1",
                                type = BooleanAttributeType
                            )
                        )
                    ),
                    { _: String, _: UUID -> false },
                    setOf("attrs"),
                    "reject duplicated attrs"
                ),

                Arguments.of(
                    Annotation(
                        id = uuid1,
                        value = SpanAnnotationValue(
                            start = 5,
                            end = 6
                        ),
                        attrs = listOf(
                            AnnotationAttribute(
                                name = "attr1",
                                value = BooleanAttributeValue(value = true)
                            )
                        )
                    ),
                    Layer(
                        name = "layer1",
                        type = SpanLayerType,
                        attrs = emptyList()
                    ),
                    { _: String, _: UUID -> false },
                    setOf("attrs"),
                    "reject attrs not defined in layer"
                ),

                Arguments.of(
                    Annotation(
                        id = uuid1,
                        value = SpanAnnotationValue(
                            start = 5,
                            end = 6
                        ),
                        attrs = emptyList()
                    ),
                    Layer(
                        name = "layer1",
                        type = SpanLayerType,
                        attrs = listOf(
                            Attribute(
                                name = "attr1",
                                type = BooleanAttributeType
                            )
                        )
                    ),
                    { _: String, _: UUID -> false },
                    setOf("attrs"),
                    "reject missing attrs"
                ),

                Arguments.of(
                    Annotation(
                        id = uuid1,
                        value = SpanAnnotationValue(
                            start = 5,
                            end = 6
                        ),
                        attrs = listOf(
                            AnnotationAttribute(
                                name = "attr1",
                                value = BooleanAttributeValue(value = true)
                            ),
                            AnnotationAttribute(
                                name = "attr2",
                                value = IntAttributeValue(value = 10)
                            ),
                            AnnotationAttribute(
                                name = "attr3",
                                value = FloatAttributeValue(value = 3.14f)
                            ),
                            AnnotationAttribute(
                                name = "attr4",
                                value = StringAttributeValue(value = "haha")
                            ),
                            AnnotationAttribute(
                                name = "attr5",
                                value = EnumAttributeValue(value = "value1")
                            )
                        )
                    ),
                    Layer(
                        name = "layer1",
                        type = SpanLayerType,
                        attrs = listOf(
                            Attribute(
                                name = "attr1",
                                type = IntAttributeType
                            ),
                            Attribute(
                                name = "attr2",
                                type = FloatAttributeType
                            ),
                            Attribute(
                                name = "attr3",
                                type = StringAttributeType
                            ),
                            Attribute(
                                name = "attr4",
                                type = EnumAttributeType(
                                    values = listOf("value1", "value2")
                                )
                            ),
                            Attribute(
                                name = "attr5",
                                type = BooleanAttributeType
                            )
                        )
                    ),
                    { _: String, _: UUID -> false },
                    setOf("attrs[0].value", "attrs[1].value", "attrs[2].value", "attrs[3].value", "attrs[4].value"),
                    "reject incompatible attr types"
                ),

                Arguments.of(
                    Annotation(
                        id = uuid1,
                        value = SpanAnnotationValue(
                            start = 5,
                            end = 6
                        ),
                        attrs = emptyList()
                    ),
                    Layer(
                        name = "layer2",
                        type = RelationLayerType(
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
                        attrs = emptyList()
                    ),
                    { _: String, _: UUID -> false },
                    setOf("value"),
                    "reject span for relation layer"
                ),

                Arguments.of(
                    Annotation(
                        id = uuid2,
                        value = RelationAnnotationValue(
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
                        attrs = emptyList()
                    ),
                    Layer(
                        name = "layer2",
                        type = SpanLayerType,
                        attrs = emptyList()
                    ),
                    { _: String, _: UUID -> false },
                    setOf("value"),
                    "reject relation for span layer"
                ),

                Arguments.of(
                    Annotation(
                        id = uuid2,
                        value = RelationAnnotationValue(
                            spanRoles = listOf(
                                RelationAnnotationSpanRole(
                                    name = "role1",
                                    targetAnnotationId = uuid1
                                ),
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
                        attrs = emptyList()
                    ),
                    Layer(
                        name = "layer2",
                        type = RelationLayerType(
                            spanRoles = listOf(
                                RelationLayerSpanRole(
                                    name = "role1",
                                    targetLayerName = "layer1"
                                ),
                                RelationLayerSpanRole(
                                    name = "role2",
                                    targetLayerName = "layer1"
                                ),
                            )
                        ),
                        attrs = emptyList()
                    ),
                    { layer: String, id: UUID -> (layer == "layer1") && (id == uuid1) },
                    setOf("value.spanRoles"),
                    "reject duplicated span roles"
                ),

                Arguments.of(
                    Annotation(
                        id = uuid2,
                        value = RelationAnnotationValue(
                            spanRoles = listOf(
                                RelationAnnotationSpanRole(
                                    name = "role1",
                                    targetAnnotationId = uuid1
                                ),
                                RelationAnnotationSpanRole(
                                    name = "role2",
                                    targetAnnotationId = uuid1
                                ),
                                RelationAnnotationSpanRole(
                                    name = "role3",
                                    targetAnnotationId = uuid1
                                )
                            )
                        ),
                        attrs = emptyList()
                    ),
                    Layer(
                        name = "layer2",
                        type = RelationLayerType(
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
                        attrs = emptyList()
                    ),
                    { layer: String, id: UUID -> (layer == "layer1") && (id == uuid1) },
                    setOf("value.spanRoles"),
                    "reject span roles not defined in layer"
                ),

                Arguments.of(
                    Annotation(
                        id = uuid2,
                        value = RelationAnnotationValue(
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
                        attrs = emptyList()
                    ),
                    Layer(
                        name = "layer2",
                        type = RelationLayerType(
                            spanRoles = listOf(
                                RelationLayerSpanRole(
                                    name = "role1",
                                    targetLayerName = "layer1"
                                ),
                                RelationLayerSpanRole(
                                    name = "role2",
                                    targetLayerName = "layer1"
                                ),
                                RelationLayerSpanRole(
                                    name = "role3",
                                    targetLayerName = "layer1"
                                )
                            )
                        ),
                        attrs = emptyList()
                    ),
                    { layer: String, id: UUID -> (layer == "layer1") && (id == uuid1) },
                    setOf("value.spanRoles"),
                    "reject missing span roles"
                ),

                Arguments.of(
                    Annotation(
                        id = uuid2,
                        value = RelationAnnotationValue(
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
                        attrs = emptyList()
                    ),
                    Layer(
                        name = "layer2",
                        type = RelationLayerType(
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
                        attrs = emptyList()
                    ),
                    { _: String, _: UUID -> false },
                    setOf("value.spanRoles[0].targetAnnotationId", "value.spanRoles[1].targetAnnotationId"),
                    "reject non existing annotation reference"
                ),

                Arguments.of(
                    Annotation(
                        id = uuid1,
                        value = SpanAnnotationValue(
                            start = 5,
                            end = 6
                        ),
                        attrs = listOf(
                            AnnotationAttribute(
                                name = "attr1",
                                value = BooleanAttributeValue(value = true)
                            ),
                            AnnotationAttribute(
                                name = "attr2",
                                value = IntAttributeValue(value = 10)
                            ),
                            AnnotationAttribute(
                                name = "attr3",
                                value = FloatAttributeValue(value = 3.14f)
                            )
                        )
                    ),
                    Layer(
                        name = "layer1",
                        type = SpanLayerType,
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
                    { _: String, _: UUID -> false },
                    emptySet<String>(),
                    "accept valid span annotation"
                ),

                Arguments.of(
                    Annotation(
                        id = uuid2,
                        value = RelationAnnotationValue(
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
                                value = StringAttributeValue(value = "haha")
                            ),
                            AnnotationAttribute(
                                name = "attr2",
                                value = EnumAttributeValue(value = "value1")
                            )
                        )
                    ),
                    Layer(
                        name = "layer2",
                        type = RelationLayerType(
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
                                type = StringAttributeType
                            ),
                            Attribute(
                                name = "attr2",
                                type = EnumAttributeType(
                                    values = listOf("value1", "value2")
                                )
                            )
                        )
                    ),
                    { layer: String, id: UUID -> (layer == "layer1") && (id == uuid1) },
                    emptySet<String>(),
                    "accept valid relation annotation"
                )
            )
        }
    }
}
