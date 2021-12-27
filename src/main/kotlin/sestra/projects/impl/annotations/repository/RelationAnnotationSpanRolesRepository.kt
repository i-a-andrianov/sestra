package sestra.projects.impl.annotations.repository

import org.springframework.data.repository.CrudRepository
import sestra.projects.impl.annotations.entities.RelationAnnotationSpanRoleEntity
import java.util.UUID

interface RelationAnnotationSpanRolesRepository : CrudRepository<RelationAnnotationSpanRoleEntity, Int> {
    fun findAllByTargetAnnotationUuid(uuid: UUID): List<RelationAnnotationSpanRoleEntity>
}
