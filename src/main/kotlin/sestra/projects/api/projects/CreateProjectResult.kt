package sestra.projects.api.projects

import sestra.common.api.ValidationError

sealed interface CreateProjectResult

object ProjectCreated : CreateProjectResult

object ProjectAlreadyExists : CreateProjectResult

data class InvalidProject(
    val errors: List<ValidationError>
) : CreateProjectResult
