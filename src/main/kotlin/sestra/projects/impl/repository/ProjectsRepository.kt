package sestra.projects.impl.repository

import org.springframework.data.repository.CrudRepository
import sestra.projects.impl.entities.ProjectEntity
import sestra.projects.impl.entities.ProjectNameOnly

interface ProjectsRepository : CrudRepository<ProjectEntity, Int> {
    fun existsByName(name: String): Boolean

    fun findByName(name: String): ProjectEntity?

    fun findNamesBy(): List<ProjectNameOnly>
}
