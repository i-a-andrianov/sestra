package sestra.projects.impl.entities

import sestra.common.impl.AbstractEntity
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.OneToMany

@Entity(name = "projects")
class ProjectEntity : AbstractEntity() {
    @Column
    var name: String? = null

    @OneToMany(fetch = FetchType.EAGER, cascade = [CascadeType.ALL], mappedBy = "project")
    var layers: Set<LayerEntity>? = null

    @Column(name = "created_by")
    var createdBy: String? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ProjectEntity) return false

        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        return name?.hashCode() ?: 0
    }
}
