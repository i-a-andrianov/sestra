package sestra.projects.api.store

import sestra.projects.api.core.Project

interface ProjectsQueries {
    fun getByName(whoami: String, name: String): Project?

    fun getNames(whoami: String): List<String>
}