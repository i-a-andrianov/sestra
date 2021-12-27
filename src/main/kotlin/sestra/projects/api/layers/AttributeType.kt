package sestra.projects.api.layers

sealed interface AttributeType

object BooleanAttributeType : AttributeType

object IntAttributeType : AttributeType

object FloatAttributeType : AttributeType

object StringAttributeType : AttributeType

data class EnumAttributeType(
    val values: List<String>
) : AttributeType
