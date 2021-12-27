package sestra.projects.api.projects

interface ProjectsStore {
    fun createProject(whoami: String, project: Project): CreateProjectResult

    fun getProjectByName(whoami: String, name: String): Project?

    fun getProjectNames(whoami: String): GetProjectsNamesResult
}
