package sestra.documents.impl.validator

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import sestra.common.api.ValidationError
import sestra.documents.api.core.Document
import java.util.stream.Stream

class DocumentValidatorTest {
    private val validator = DocumentValidator()

    @ParameterizedTest(name = "{displayName} {2}")
    @ArgumentsSource(TestCases::class)
    @Suppress("UNUSED_PARAMETER")
    fun `validator should`(document: Document, expectedFields: Set<String>, testCase: String) {
        val actualErrors = validator.validate(document)
        val actualFields = actualErrors.map(ValidationError::field).toSet()
        Assertions.assertEquals(expectedFields, actualFields)
    }

    private class TestCases : ArgumentsProvider {
        override fun provideArguments(context: ExtensionContext?): Stream<out Arguments> {
            return Stream.of(
                Arguments.of(
                    Document(
                        projectName = "project",
                        name = "    ",
                        text = " "
                    ),
                    setOf("name", "text"),
                    "reject blank name and text"
                ),
                Arguments.of(
                    Document(
                        projectName = "project",
                        name = "document",
                        text = "Hello, world!"
                    ),
                    emptySet<String>(),
                    "accept valid document"
                )
            )
        }
    }
}
