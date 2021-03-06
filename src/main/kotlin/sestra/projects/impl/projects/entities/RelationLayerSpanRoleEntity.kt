package sestra.projects.impl.projects.entities

import sestra.common.impl.AbstractEntity
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne

@Entity(name = "relation_layer_span_roles")
class RelationLayerSpanRoleEntity : AbstractEntity() {
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "layer_id", nullable = false)
    var layer: LayerEntity? = null

    @Column(name = "in_layer_index")
    var inLayerIndex: Int? = null

    @Column
    var name: String? = null

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "target_layer_id", nullable = false)
    var targetLayer: LayerEntity? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RelationLayerSpanRoleEntity) return false

        if (layer != other.layer) return false
        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        var result = layer?.hashCode() ?: 0
        result = 31 * result + (name?.hashCode() ?: 0)
        return result
    }
}
