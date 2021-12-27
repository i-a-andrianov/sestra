package sestra.projects.impl.annotations.entities

import sestra.common.impl.AbstractEntity
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne

@Entity(name = "relation_annotation_span_roles")
class RelationAnnotationSpanRoleEntity : AbstractEntity() {
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "annotation_id")
    var annotation: AnnotationEntity? = null

    @Column(name = "in_annotation_index")
    var inAnnotationIndex: Int? = null

    @Column
    var name: String? = null

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "target_annotation_id")
    var targetAnnotation: AnnotationEntity? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RelationAnnotationSpanRoleEntity) return false

        if (annotation != other.annotation) return false
        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        var result = annotation?.hashCode() ?: 0
        result = 31 * result + (name?.hashCode() ?: 0)
        return result
    }
}
