package sestra.documents.api.store

import sestra.documents.api.core.Document

interface DocumentsQueries {
    fun getByName(whoami: String, projectName: String, name: String): Document?

    fun getNames(whoami: String, projectName: String): GetDocumentsNamesResult
}

data class GetDocumentsNamesResult(
    val names: List<String>
)
