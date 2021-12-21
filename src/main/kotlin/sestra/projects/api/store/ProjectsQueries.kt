package sestra.projects.api.store

import sestra.projects.api.core.Project

interface ProjectsQueries {
    fun getByName(whoami: String, name: String): Project?

    fun getNames(whoami: String): GetProjectsNamesResult
}

data class GetProjectsNamesResult(
    val names: List<String>
)
