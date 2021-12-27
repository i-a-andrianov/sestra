package sestra.projects.api.annotations

sealed interface AttributeValue {
    val value: Any
}

data class BooleanAttributeValue(
    override val value: Boolean
) : AttributeValue

data class IntAttributeValue(
    override val value: Int
) : AttributeValue

data class FloatAttributeValue(
    override val value: Float
) : AttributeValue

data class StringAttributeValue(
    override val value: String
) : AttributeValue

data class EnumAttributeValue(
    override val value: String
) : AttributeValue
