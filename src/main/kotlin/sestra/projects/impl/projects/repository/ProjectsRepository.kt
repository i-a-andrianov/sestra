package sestra.projects.impl.projects.repository

import org.springframework.data.repository.CrudRepository
import sestra.projects.impl.projects.entities.ProjectEntity
import sestra.projects.impl.projects.entities.ProjectIdOnly
import sestra.projects.impl.projects.entities.ProjectNameOnly

interface ProjectsRepository : CrudRepository<ProjectEntity, Int> {
    fun existsByName(name: String): Boolean

    fun findIdByName(name: String): ProjectIdOnly?

    fun findByName(name: String): ProjectEntity?

    fun findNamesBy(): List<ProjectNameOnly>
}
