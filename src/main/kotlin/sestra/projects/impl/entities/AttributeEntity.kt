package sestra.projects.impl.entities

import sestra.common.impl.AbstractEntity
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.OneToMany

@Entity(name = "attributes")
class AttributeEntity : AbstractEntity() {
    @ManyToOne(fetch = FetchType.EAGER, cascade = [CascadeType.ALL])
    @JoinColumn(name = "layer_id")
    var layer: LayerEntity? = null

    @Column(name = "in_layer_index")
    var inLayerIndex: Int? = null

    @Column
    var name: String? = null

    @Column
    var type: String? = null

    @OneToMany(fetch = FetchType.EAGER, cascade = [CascadeType.ALL], mappedBy = "attribute")
    var enumValues: Set<EnumAttributeValueEntity>? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AttributeEntity) return false

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
