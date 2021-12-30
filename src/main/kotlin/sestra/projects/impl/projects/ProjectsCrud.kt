package sestra.projects.impl.projects

import sestra.projects.api.layers.Layer
import sestra.projects.api.projects.CreateProjectResult
import sestra.projects.api.projects.GetProjectsNamesResult
import sestra.projects.api.projects.Project
import sestra.projects.impl.projects.mapper.ProjectFromEntityMapper
import sestra.projects.impl.projects.mapper.ProjectToEntityMapper
import sestra.projects.impl.projects.repository.LayersRepository
import sestra.projects.impl.projects.repository.ProjectsRepository
import sestra.projects.impl.projects.validator.ProjectValidator

class ProjectsCrud(
    private val repo: ProjectsRepository,
    private val layersRepo: LayersRepository
) {
    private val validator = ProjectValidator()
    private val toMapper = ProjectToEntityMapper()
    private val fromMapper = ProjectFromEntityMapper()

    fun create(project: Project, createdBy: String): CreateProjectResult {
        val errors = validator.validate(project)
        if (errors.isNotEmpty()) {
            return CreateProjectResult.InvalidProject(errors)
        }

        if (repo.existsByName(project.name)) {
            return CreateProjectResult.ProjectAlreadyExists
        }

        val entity = toMapper.toEntity(project, createdBy)
        repo.save(entity)
        return CreateProjectResult.ProjectCreated
    }

    fun getLayerIdByName(projectName: String, name: String): Int? {
        return layersRepo.findIdByProjectNameAndName(projectName, name)?.id
    }

    fun getLayerWithIdByName(projectName: String, name: String): Pair<Int, Layer>? {
        val entity = layersRepo.findByProjectNameAndName(projectName, name)
        if (entity === null) {
            return null
        }
        return Pair(entity.id!!, fromMapper.fromEntity(entity))
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
