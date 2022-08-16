package com.piratesofcode.spyglass.api

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.piratesofcode.spyglass.model.document.Document
import com.piratesofcode.spyglass.model.document.DocumentStatus
import com.piratesofcode.spyglass.model.user.Accountant
import com.piratesofcode.spyglass.model.user.User
import com.piratesofcode.spyglass.model.user.UserRole
import com.piratesofcode.spyglass.utils.DocumentsHelper
import kotlinx.coroutines.tasks.await


object DocumentRepository {

    var firstRun = true
    var historySwitchPersonal = true

    private val databaseDocuments = Firebase.firestore.collection("documents")
    private val databaseDocumentStatus = Firebase.firestore.collection("documentStatus")
    private val databaseDocumentByUser = Firebase.firestore.collection("documentByUser")

    suspend fun getDocumentByID(id: String): Document {
        val documentSnapshot = databaseDocuments.document(id).get().await()
        val documentStatusSnapshot = databaseDocumentStatus.document(id).get().await()
        val documentByUserSnapshot = databaseDocumentByUser.document(id).get().await()

        return DocumentsHelper.createDocumentFromSnapshot(
            documentSnapshot,
            documentStatusSnapshot,
            documentByUserSnapshot
        )
    }

    suspend fun getDocumentsByUserId(userId: String): List<Document> {
        val documents: MutableList<Document> = mutableListOf()
        val documentsQuerySnapshot = databaseDocuments.whereEqualTo("user", userId).get().await()

        for (documentSnapshot in documentsQuerySnapshot) {
            val documentStatusSnapshot =
                databaseDocumentStatus.document(documentSnapshot.id).get().await()
            val documentByUserSnapshot =
                databaseDocumentByUser.document(documentSnapshot.id).get().await()
            if (documentStatusSnapshot["scannedProperly"].toString()
                    .toBoolean() && !documentStatusSnapshot["archived"].toString().toBoolean()
            ) {
                val document = DocumentsHelper.createDocumentFromSnapshot(
                    documentSnapshot,
                    documentStatusSnapshot,
                    documentByUserSnapshot
                )
                documents.add(document)
            }
        }

        return documents
    }

    suspend fun getDocumentsByStatus(status: DocumentStatus): List<Document> {
        val documents: MutableList<Document> = mutableListOf()
        val documentStatusQuerySnapshot = databaseDocumentStatus
            .whereEqualTo("archived", status.archived)
            .whereEqualTo("signed", status.signed)
            .whereEqualTo("toBeSigned", status.toBeSigned)
            .whereEqualTo("revised", status.revised)
            .whereEqualTo("scannedProperly", status.scannedProperly)
            .get().await()

        val isAccountant = AuthenticationRepository.user!!.role == UserRole.ACCOUNTANT
        val specialization = if (isAccountant)
            (AuthenticationRepository.user!! as Accountant).specialization else null

        for (documentStatusSnapshot in documentStatusQuerySnapshot) {
            val documentSnapshot =
                databaseDocuments.document(documentStatusSnapshot.id).get().await()
            val documentByUserSnapshot =
                databaseDocumentByUser.document(documentSnapshot.id).get().await()
            val document = DocumentsHelper
                .createDocumentFromSnapshot(
                    documentSnapshot,
                    documentStatusSnapshot,
                    documentByUserSnapshot
                )

            if (isAccountant && document.type.toString() == specialization.toString()) {
                documents.add(document)
                continue
            }
            if (!isAccountant) {
                documents.add(document)
                continue
            }
        }

        return documents
    }

    fun saveDocument(
        document: Document,
        user: User = AuthenticationRepository.user!!
    ): Task<DocumentReference> {
        val toSave = DocumentsHelper.createMapForSaving(document)
        toSave["user"] = user.id
        return databaseDocuments.add(toSave)
    }

    fun updateDocumentStatus(document: Document): Task<Void> {
        return databaseDocumentStatus.document(document.id)
            .set(DocumentsHelper.createDocStatus(document), SetOptions.merge())
    }

    fun updateDocumentByUser(document: Document): Task<Void> {
        return databaseDocumentByUser.document(document.id)
            .set(DocumentsHelper.createDocByUser(document), SetOptions.merge())
    }

}
