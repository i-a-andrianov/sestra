package sestra.projects.rest.annotations

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import sestra.projects.api.annotations.Annotation
import sestra.projects.api.annotations.AnnotationContainer
import sestra.projects.api.annotations.AnnotationsStore
import sestra.projects.api.annotations.CreateAnnotationResult
import sestra.projects.api.annotations.DeleteAnnotationResult
import java.security.Principal
import java.util.UUID

@RestController
@RequestMapping("/api/projects/annotations")
class AnnotationsStoreRestController(
    private val store: AnnotationsStore
) {
    @PostMapping
    fun create(
        principal: Principal,
        @RequestParam projectName: String,
        @RequestParam documentName: String,
        @RequestParam layerName: String,
        @RequestBody annotation: Annotation
    ): ResponseEntity<Any> {
        val container = AnnotationContainer(projectName, documentName, layerName)
        val result = store.createAnnotation(principal.name, container, annotation)

        return when (result) {
            is CreateAnnotationResult.AnnotationCreated -> ResponseEntity.ok().body(emptyMap<Any, Any>())
            is CreateAnnotationResult.ProjectNotFound -> ResponseEntity.badRequest()
                .body(mapOf("description" to "Project with given name doesn't exist"))
            is CreateAnnotationResult.DocumentNotFound -> ResponseEntity.badRequest()
                .body(mapOf("description" to "Document with given name doesn't exist"))
            is CreateAnnotationResult.LayerNotFound -> ResponseEntity.badRequest()
                .body(mapOf("description" to "Layer with given name doesn't exist"))
            is CreateAnnotationResult.AnnotationAlreadyExists -> ResponseEntity.badRequest()
                .body(mapOf("description" to "Annotation with same id already exists"))
            is CreateAnnotationResult.InvalidAnnotation -> ResponseEntity.badRequest().body(result)
        }
    }

    @GetMapping
    fun get(
        principal: Principal,
        @RequestParam projectName: String,
        @RequestParam documentName: String,
        @RequestParam layerName: String
    ): ResponseEntity<Any> {
        val container = AnnotationContainer(projectName, documentName, layerName)
        val result = store.getAnnotations(principal.name, container)

        return ResponseEntity.ok().body(result)
    }

    @DeleteMapping("/{id}")
    fun delete(principal: Principal, @PathVariable id: UUID): ResponseEntity<Any> {
        val result = store.deleteAnnotation(principal.name, id)

        return when (result) {
            is DeleteAnnotationResult.AnnotationDeleted -> ResponseEntity.ok().body(emptyMap<Any, Any>())
            is DeleteAnnotationResult.AnnotationIsReferencedByOthers -> ResponseEntity.badRequest().body(result)
            is DeleteAnnotationResult.AnnotationNotFound -> ResponseEntity.notFound().build()
        }
    }
}
