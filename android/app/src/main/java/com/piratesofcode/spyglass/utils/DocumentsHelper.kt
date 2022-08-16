package com.piratesofcode.spyglass.utils

import com.google.firebase.firestore.DocumentSnapshot
import com.piratesofcode.spyglass.model.document.*
import java.util.*

object DocumentsHelper {

    fun createMapForSaving(document: Document): HashMap<String, Any> {
        val returnMap: HashMap<String, Any> = when (document.type) {
            DocumentType.RECEIPT -> createReceipt(document as Receipt)
            DocumentType.OFFER -> createOffer(document as Offer)
            DocumentType.INTERNAL -> createInternal(document as InternalDocument)
        }

        return returnMap
    }

    fun createDocumentFromSnapshot(
        documentSnapshot: DocumentSnapshot,
        documentStatusSnapshot: DocumentSnapshot,
        documentByUserSnapshot: DocumentSnapshot
    ) =
        when (documentSnapshot["type"]) {
            DocumentType.RECEIPT.value.lowercase() -> Receipt(
                documentSnapshot.id,
                documentSnapshot["timestamp"].toString().toLong(),
                documentSnapshot["label"].toString(),
                createDocumentStatusFromSnapshot(documentStatusSnapshot),
                createDocumentByUserFromSnapshot(documentByUserSnapshot),
                clientName = documentSnapshot["clientName"].toString(),
                items = createItemsFromSnapshot(documentSnapshot),
                totalPrice = documentSnapshot["totalPrice"].toString().toDouble()
            )
            DocumentType.OFFER.value.lowercase() -> Offer(
                documentSnapshot.id,
                documentSnapshot["timestamp"].toString().toLong(),
                documentSnapshot["label"].toString(),
                createDocumentStatusFromSnapshot(documentStatusSnapshot),
                createDocumentByUserFromSnapshot(documentByUserSnapshot),
                items = createItemsFromSnapshot(documentSnapshot),
                totalPrice = documentSnapshot["totalPrice"].toString().toDouble()
            )
            DocumentType.INTERNAL.value.lowercase() -> InternalDocument(
                documentSnapshot.id,
                documentSnapshot["timestamp"].toString().toLong(),
                documentSnapshot["label"].toString(),
                createDocumentStatusFromSnapshot(documentStatusSnapshot),
                createDocumentByUserFromSnapshot(documentByUserSnapshot),
                content = documentSnapshot["content"].toString()
            )
            else -> throw IllegalStateException("Invalid document type")
        }

    private fun createDocumentStatusFromSnapshot(snapshot: DocumentSnapshot) =
        DocumentStatus(
            snapshot["archived"].toString().toBoolean(),
            snapshot["signed"].toString().toBoolean(),
            snapshot["toBeSigned"].toString().toBoolean(),
            snapshot["revised"].toString().toBoolean(),
            snapshot["scannedProperly"].toString().toBoolean()
        )

    private fun createDocumentByUserFromSnapshot(snapshot: DocumentSnapshot) =
        DocumentByUser(
            snapshot["archivedBy"].toString(),
            snapshot["revisedBy"].toString(),
            snapshot["sentToSignBy"].toString(),
            snapshot["signedBy"].toString()
        )

    @Suppress("UNCHECKED_CAST")
    private fun createItemsFromSnapshot(documentSnapshot: DocumentSnapshot): HashMap<String, Double> {
        return documentSnapshot["items"] as HashMap<String, Double>
    }

    private fun createReceipt(receipt: Receipt): HashMap<String, Any> {
        return hashMapOf(
            "clientName" to receipt.clientName,
            "timestamp" to receipt.timestamp,
            "items" to receipt.items,
            "label" to receipt.label,
            "totalPrice" to receipt.totalPrice,
            "type" to receipt.type.toString().lowercase(Locale.getDefault())
        )
    }

    private fun createOffer(offer: Offer): HashMap<String, Any> {
        return hashMapOf(
            "timestamp" to offer.timestamp,
            "items" to offer.items,
            "label" to offer.label,
            "totalPrice" to offer.totalPrice,
            "type" to offer.type.toString().lowercase(Locale.getDefault())
        )
    }

    private fun createInternal(internalDocument: InternalDocument): HashMap<String, Any> {
        return hashMapOf(
            "content" to internalDocument.content,
            "timestamp" to internalDocument.timestamp,
            "label" to internalDocument.label,
            "type" to internalDocument.type.toString().lowercase(Locale.getDefault())
        )

    }

    fun createDocStatus(document: Document): HashMap<String, Any> {
        val docStatus = document.documentStatus
        return hashMapOf(
            "archived" to docStatus.archived,
            "revised" to docStatus.revised,
            "scannedProperly" to docStatus.scannedProperly,
            "toBeSigned" to docStatus.toBeSigned,
            "signed" to docStatus.signed
        )
    }

    fun createDocByUser(document: Document): HashMap<String, Any> {
        val docByUser = document.documentByUser
        return hashMapOf(
            "archivedBy" to docByUser.archivedBy,
            "revisedBy" to docByUser.revisedBy,
            "sentToSignBy" to docByUser.sentToSignBy,
            "signedBy" to docByUser.signedBy
        )
    }
}
