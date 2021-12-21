package sestra.projects.api.store

import sestra.common.api.ValidationError
import sestra.projects.api.core.Project

interface ProjectsCommands {
    fun create(whoami: String, project: Project): CreateProjectResult
}

sealed interface CreateProjectResult

object ProjectCreated : CreateProjectResult

object ProjectAlreadyExists : CreateProjectResult

data class InvalidProject(
    val errors: List<ValidationError>
) : CreateProjectResult
