package sestra.projects.api.layers

sealed interface AttributeType {
    object Boolean : AttributeType

    object Int : AttributeType

    object Float : AttributeType

    object String : AttributeType

    data class Enum(
        val values: List<kotlin.String>
    ) : AttributeType
}
