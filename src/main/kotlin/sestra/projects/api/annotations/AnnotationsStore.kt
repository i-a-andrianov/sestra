package sestra.projects.api.annotations

import java.util.UUID

interface AnnotationsStore {
    fun createAnnotation(whoami: String, container: AnnotationContainer, annotation: Annotation): CreateAnnotationResult

    fun getAnnotations(whoami: String, container: AnnotationContainer): List<Annotation>

    fun deleteAnnotation(whoami: String, id: UUID): DeleteAnnotationResult
}
