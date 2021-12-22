package sestra.documents.rest

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import sestra.documents.api.core.Document
import sestra.documents.api.store.DocumentAlreadyExists
import sestra.documents.api.store.DocumentCreated
import sestra.documents.api.store.DocumentsStore
import sestra.documents.api.store.InvalidDocument
import sestra.documents.api.store.ProjectNotFound
import java.security.Principal

@RestController
@RequestMapping("/api/documents")
class DocumentsStoreRestController(
    private val store: DocumentsStore
) {
    @PostMapping
    fun create(principal: Principal, @RequestBody document: Document): ResponseEntity<Any> {
        val result = store.create(principal.name, document)

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
        val document = store.getByName(principal.name, projectName, name)

        return when (document) {
            null -> ResponseEntity.notFound().build()
            else -> ResponseEntity.ok().body(document)
        }
    }

    @GetMapping("/names")
    fun getNames(principal: Principal, @RequestParam projectName: String): ResponseEntity<Any> {
        val result = store.getNames(principal.name, projectName)

        return ResponseEntity.ok().body(result)
    }
}
