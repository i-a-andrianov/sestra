package sestra.projects.api.store

import sestra.common.api.ValidationError

sealed interface CreateResult

object CreateSuccess : CreateResult

data class CreateInvalidProjectError(
    val errors: List<ValidationError>
) : CreateResult

object CreateAlreadyExistsError : CreateResult
