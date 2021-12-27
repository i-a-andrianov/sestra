package sestra.projects.api.documents

interface DocumentsStore {
    fun createDocument(whoami: String, container: DocumentContainer, document: Document): CreateDocumentResult

    fun getDocumentByName(whoami: String, container: DocumentContainer, name: String): Document?

    fun getDocumentNames(whoami: String, container: DocumentContainer): GetDocumentsNamesResult
}
