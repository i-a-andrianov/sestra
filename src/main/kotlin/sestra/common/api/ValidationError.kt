package sestra.common.api

data class ValidationError(
    val field: String,
    val description: String
)
