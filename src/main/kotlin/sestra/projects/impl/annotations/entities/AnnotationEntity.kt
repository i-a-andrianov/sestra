package sestra.projects.impl.annotations.entities

import sestra.common.impl.AbstractEntity
import java.util.UUID
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.OneToMany

@Entity(name = "annotations")
class AnnotationEntity : AbstractEntity() {
    @Column
    var uuid: UUID? = null

    @Column(name = "document_id")
    var documentId: Int? = null

    @Column(name = "layer_id")
    var layerId: Int? = null

    @Column
    var type: String? = null

    @Column(name = "span_start")
    var spanStart: Int? = null

    @Column(name = "span_end")
    var spanEnd: Int? = null

    @OneToMany(fetch = FetchType.EAGER, cascade = [CascadeType.ALL], mappedBy = "annotation")
    var relationSpanRoles: Set<RelationAnnotationSpanRoleEntity>? = null

    @OneToMany(fetch = FetchType.EAGER, cascade = [CascadeType.ALL], mappedBy = "annotation")
    var attrs: Set<AnnotationAttributeEntity>? = null

    @Column(name = "created_by")
    var createdBy: String? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AnnotationEntity) return false

        if (uuid != other.uuid) return false

        return true
    }

    override fun hashCode(): Int {
        return uuid?.hashCode() ?: 0
    }
}
