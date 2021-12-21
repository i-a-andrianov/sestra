package sestra.projects.impl

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import sestra.projects.api.core.Project
import sestra.projects.api.store.CreateProjectResult
import sestra.projects.api.store.GetProjectsNamesResult
import sestra.projects.api.store.InvalidProject
import sestra.projects.api.store.ProjectAlreadyExists
import sestra.projects.api.store.ProjectCreated
import sestra.projects.api.store.ProjectsStore
import sestra.projects.impl.mapper.FromEntityMapper
import sestra.projects.impl.mapper.ToEntityMapper
import sestra.projects.impl.repository.ProjectsRepository
import sestra.projects.impl.validator.ProjectValidator

@Component
class ProjectsStoreImpl(
    private val repository: ProjectsRepository
) : ProjectsStore {
    private val validator = ProjectValidator()
    private val toEntityMapper = ToEntityMapper()
    private val fromEntityMapper = FromEntityMapper()

    @Transactional
    override fun create(whoami: String, project: Project): CreateProjectResult {
        val errors = validator.validate(project)
        if (errors.isNotEmpty()) {
            return InvalidProject(errors)
        }

        if (repository.existsByName(project.name)) {
            return ProjectAlreadyExists
        }

        val entity = toEntityMapper.toEntity(project, whoami)
        repository.save(entity)
        return ProjectCreated
    }

    @Transactional(readOnly = true)
    override fun getByName(whoami: String, name: String): Project? {
        val entity = repository.findByName(name)
        return entity?.let { fromEntityMapper.fromEntity(it) }
    }

    @Transactional(readOnly = true)
    override fun getNames(whoami: String): GetProjectsNamesResult {
        // TODO load just names
        val entities = repository.findAll()
        val names = entities.map { it.name!! }
        return GetProjectsNamesResult(names)
    }
}
