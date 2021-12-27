package sestra.projects.impl.projects

import sestra.projects.api.projects.CreateProjectResult
import sestra.projects.api.projects.GetProjectsNamesResult
import sestra.projects.api.projects.InvalidProject
import sestra.projects.api.projects.Project
import sestra.projects.api.projects.ProjectAlreadyExists
import sestra.projects.api.projects.ProjectCreated
import sestra.projects.impl.projects.mapper.ProjectFromEntityMapper
import sestra.projects.impl.projects.mapper.ProjectToEntityMapper
import sestra.projects.impl.projects.repository.ProjectsRepository
import sestra.projects.impl.projects.validator.ProjectValidator

class ProjectsCrud(
    private val repo: ProjectsRepository
) {
    private val validator = ProjectValidator()
    private val toMapper = ProjectToEntityMapper()
    private val fromMapper = ProjectFromEntityMapper()

    fun create(project: Project, createdBy: String): CreateProjectResult {
        val errors = validator.validate(project)
        if (errors.isNotEmpty()) {
            return InvalidProject(errors)
        }

        if (repo.existsByName(project.name)) {
            return ProjectAlreadyExists
        }

        val entity = toMapper.toEntity(project, createdBy)
        repo.save(entity)
        return ProjectCreated
    }

    fun getIdByName(name: String): Int? {
        return repo.findIdByName(name)?.id
    }

    fun getByName(name: String): Project? {
        val entity = repo.findByName(name)
        return entity?.let { fromMapper.fromEntity(it) }
    }

    fun getNames(): GetProjectsNamesResult {
        val names = repo.findNamesBy().map { it.name }
        return GetProjectsNamesResult(names)
    }
}
