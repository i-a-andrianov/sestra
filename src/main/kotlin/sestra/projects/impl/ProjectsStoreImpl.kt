package sestra.projects.impl

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import sestra.projects.api.core.Project
import sestra.projects.api.store.CreateAlreadyExistsError
import sestra.projects.api.store.CreateInvalidProjectError
import sestra.projects.api.store.CreateResult
import sestra.projects.api.store.CreateSuccess
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
    override fun create(whoami: String, project: Project): CreateResult {
        val errors = validator.validate(project)
        if (errors.isNotEmpty()) {
            return CreateInvalidProjectError(errors)
        }

        if (repository.existsByName(project.name)) {
            return CreateAlreadyExistsError
        }

        val entity = toEntityMapper.toEntity(project, whoami)
        repository.save(entity)
        return CreateSuccess
    }

    @Transactional(readOnly = true)
    override fun getByName(whoami: String, name: String): Project? {
        val entity = repository.findByName(name)
        return entity?.let { fromEntityMapper.fromEntity(it) }
    }

    @Transactional(readOnly = true)
    override fun getNames(whoami: String): List<String> {
        // TODO load just names
        val entities = repository.findAll()
        return entities.map { it.name!! }
    }
}
