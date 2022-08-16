package com.piratesofcode.spyglass.ui.document

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.piratesofcode.spyglass.api.DocumentRepository
import com.piratesofcode.spyglass.model.document.Document
import com.piratesofcode.spyglass.model.document.DocumentAction
import kotlinx.coroutines.launch

class DocumentViewModel : ViewModel() {

    private var documentsLiveData = MutableLiveData<Task<DocumentReference>>()
    private var statusLiveData = MutableLiveData<String>()

    fun getDocumentsLiveData(): LiveData<Task<DocumentReference>> {
        return documentsLiveData
    }

    fun getStatusLiveData(): LiveData<String> {
        return statusLiveData
    }

    fun saveDocument(document: Document) {
        documentsLiveData.value = DocumentRepository.saveDocument(document)
    }

    fun updateDocument(document: Document, documentAction: DocumentAction) {
        viewModelScope.launch {
            val remoteDocument = DocumentRepository.getDocumentByID(document.id)

            val updatePossible = when (documentAction) {
                DocumentAction.SCAN -> true
                DocumentAction.REVISE -> !remoteDocument.documentStatus.revised
                DocumentAction.SEND_TO_SIGN -> !remoteDocument.documentStatus.archived
                        && !remoteDocument.documentStatus.toBeSigned
                DocumentAction.SIGN -> !remoteDocument.documentStatus.signed
                DocumentAction.ARCHIVE -> !remoteDocument.documentStatus.archived
                        && (!remoteDocument.documentStatus.toBeSigned || remoteDocument.documentStatus.signed)
            }

            if (updatePossible) {
                DocumentRepository.updateDocumentStatus(document)
                DocumentRepository.updateDocumentByUser(document)

                statusLiveData.value = when (documentAction) {
                    DocumentAction.SCAN -> "Document saved."
                    DocumentAction.REVISE -> "Document revised."
                    DocumentAction.SEND_TO_SIGN -> "Document sent for signing."
                    DocumentAction.SIGN -> "Document signed."
                    DocumentAction.ARCHIVE -> "Document archived."
                }
            } else {
                statusLiveData.value = when (documentAction) {
                    DocumentAction.SCAN -> "Document couldn't be saved."
                    DocumentAction.REVISE -> "Document couldn't be revised."
                    DocumentAction.SEND_TO_SIGN -> "Document couldn't be sent for signing."
                    DocumentAction.SIGN -> "Document couldn't be signed."
                    DocumentAction.ARCHIVE -> "Document couldn't be archived."
                }
            }

        }
    }

}