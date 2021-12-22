package sestra.documents.impl.entities

import sestra.common.impl.AbstractEntity
import javax.persistence.Column
import javax.persistence.Entity

@Entity(name = "documents")
class DocumentEntity : AbstractEntity() {
    @Column(name = "project_name")
    var projectName: String? = null

    @Column
    var name: String? = null

    @Column
    var text: String? = null

    @Column(name = "created_by")
    var createdBy: String? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DocumentEntity) return false

        if (projectName != other.projectName) return false
        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        var result = projectName?.hashCode() ?: 0
        result = 31 * result + (name?.hashCode() ?: 0)
        return result
    }
}
