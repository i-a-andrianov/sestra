package sestra.projects.impl.projects.repository

import org.springframework.data.repository.CrudRepository
import sestra.projects.impl.projects.entities.LayerEntity
import sestra.projects.impl.projects.entities.LayerIdOnly

interface LayersRepository : CrudRepository<LayerEntity, Int> {
    fun findIdByProjectNameAndName(projectName: String, name: String): LayerIdOnly?

    fun findByProjectNameAndName(projectName: String, name: String): LayerEntity?
}
