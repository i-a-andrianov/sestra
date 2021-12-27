package sestra.projects.rest.documents

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import sestra.projects.api.documents.Document
import sestra.projects.api.documents.DocumentAlreadyExists
import sestra.projects.api.documents.DocumentContainer
import sestra.projects.api.documents.DocumentCreated
import sestra.projects.api.documents.DocumentsStore
import sestra.projects.api.documents.InvalidDocument
import sestra.projects.api.documents.ProjectNotFound
import java.security.Principal

@RestController
@RequestMapping("/api/projects/documents")
class DocumentsStoreRestController(
    private val store: DocumentsStore
) {
    @PostMapping
    fun create(
        principal: Principal,
        @RequestParam projectName: String,
        @RequestBody document: Document
    ): ResponseEntity<Any> {
        val container = DocumentContainer(projectName)
        val result = store.createDocument(principal.name, container, document)

        return when (result) {
            DocumentCreated -> ResponseEntity.ok().body(emptyMap<Any, Any>())
            is InvalidDocument -> ResponseEntity.badRequest().body(result)
            ProjectNotFound -> ResponseEntity.badRequest()
                .body(mapOf("description" to "Project with given name doesn't exist"))
            DocumentAlreadyExists -> ResponseEntity.badRequest()
                .body(mapOf("description" to "Document with same name already exists in given project"))
        }
    }

    @GetMapping
    fun getByName(
        principal: Principal,
        @RequestParam projectName: String,
        @RequestParam name: String
    ): ResponseEntity<Any> {
        val container = DocumentContainer(projectName)
        val document = store.getDocumentByName(principal.name, container, name)

        return when (document) {
            null -> ResponseEntity.notFound().build()
            else -> ResponseEntity.ok().body(document)
        }
    }

    @GetMapping("/names")
    fun getNames(principal: Principal, @RequestParam projectName: String): ResponseEntity<Any> {
        val container = DocumentContainer(projectName)
        val result = store.getDocumentNames(principal.name, container)

        return ResponseEntity.ok().body(result)
    }
}
