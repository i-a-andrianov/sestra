package sestra.projects.api.annotations

sealed interface AttributeValue {
    val value: Any

    data class Boolean(
        override val value: kotlin.Boolean
    ) : AttributeValue

    data class Int(
        override val value: kotlin.Int
    ) : AttributeValue

    data class Float(
        override val value: kotlin.Float
    ) : AttributeValue

    data class String(
        override val value: kotlin.String
    ) : AttributeValue

    data class Enum(
        override val value: kotlin.String
    ) : AttributeValue
}
