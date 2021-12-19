package sestra.projects.rest

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import sestra.projects.api.core.Project
import sestra.projects.api.store.CreateAlreadyExistsError
import sestra.projects.api.store.CreateInvalidProjectError
import sestra.projects.api.store.CreateSuccess
import sestra.projects.api.store.ProjectsStore
import java.security.Principal

@RestController
@RequestMapping("/api/projects")
class ProjectsStoreRestController(
    private val store: ProjectsStore
) {
    @PostMapping
    fun create(principal: Principal, @RequestBody project: Project): ResponseEntity<Any> {
        val result = store.create(principal.name, project)

        return when (result) {
            CreateSuccess -> ResponseEntity.ok().body(emptyMap<Any, Any>())
            is CreateInvalidProjectError -> ResponseEntity.badRequest().body(result)
            CreateAlreadyExistsError -> ResponseEntity.badRequest()
                .body(mapOf("description" to "Project with same name already exists"))
        }
    }

    @GetMapping
    fun getByName(principal: Principal, @RequestParam name: String): ResponseEntity<Any> {
        val project = store.getByName(principal.name, name)

        if (project === null) {
            return ResponseEntity.notFound().build()
        }
        return ResponseEntity.ok().body(project)
    }

    @GetMapping("/names")
    fun getNames(principal: Principal): ResponseEntity<Any> {
        val names = store.getNames(principal.name)

        return ResponseEntity.ok().body(mapOf("names" to names))
    }
}
