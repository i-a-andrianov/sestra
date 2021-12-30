package sestra.projects.impl.annotations.entities

import sestra.common.impl.AbstractEntity
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne

@Entity(name = "annotation_attributes")
class AnnotationAttributeEntity : AbstractEntity() {
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "annotation_id", nullable = false)
    var annotation: AnnotationEntity? = null

    @Column(name = "in_annotation_index")
    var inAnnotationIndex: Int? = null

    @Column
    var name: String? = null

    @Column
    var type: String? = null

    @Column(name = "boolean_value")
    var booleanValue: Boolean? = null

    @Column(name = "int_value")
    var intValue: Int? = null

    @Column(name = "float_value")
    var floatValue: Float? = null

    @Column(name = "string_value")
    var stringValue: String? = null

    @Column(name = "enum_value")
    var enumValue: String? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AnnotationAttributeEntity) return false

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
