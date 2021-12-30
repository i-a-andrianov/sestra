package sestra.projects.impl.projects.entities

import sestra.common.impl.AbstractEntity
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.OneToMany

@Entity(name = "layers")
class LayerEntity : AbstractEntity() {
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "project_id", nullable = false)
    var project: ProjectEntity? = null

    @Column(name = "in_project_index")
    var inProjectIndex: Int? = null

    @Column
    var name: String? = null

    @Column
    var type: String? = null

    @OneToMany(fetch = FetchType.EAGER, cascade = [CascadeType.ALL], mappedBy = "layer")
    var relationSpanRoles: Set<RelationLayerSpanRoleEntity>? = null

    @OneToMany(fetch = FetchType.EAGER, cascade = [CascadeType.ALL], mappedBy = "layer")
    var attributes: Set<AttributeEntity>? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LayerEntity) return false

        if (project != other.project) return false
        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        var result = project?.hashCode() ?: 0
        result = 31 * result + (name?.hashCode() ?: 0)
        return result
    }
}
