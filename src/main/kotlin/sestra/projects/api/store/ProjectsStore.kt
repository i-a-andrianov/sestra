package sestra.projects.api.store

import sestra.projects.api.core.Project

interface ProjectsStore : ProjectsQueries {
    fun create(whoami: String, project: Project): CreateResult
}
