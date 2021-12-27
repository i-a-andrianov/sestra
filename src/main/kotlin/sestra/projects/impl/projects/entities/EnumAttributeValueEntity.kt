package sestra.projects.impl.projects.entities

import sestra.common.impl.AbstractEntity
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne

@Entity(name = "enum_attribute_values")
class EnumAttributeValueEntity : AbstractEntity() {
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "attribute_id")
    var attribute: AttributeEntity? = null

    @Column(name = "in_attribute_index")
    var inAttributeIndex: Int? = null

    @Column
    var name: String? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is EnumAttributeValueEntity) return false

        if (attribute != other.attribute) return false
        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        var result = attribute?.hashCode() ?: 0
        result = 31 * result + (name?.hashCode() ?: 0)
        return result
    }
}
