package com.piratesofcode.spyglass.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.piratesofcode.spyglass.api.DocumentRepository
import com.piratesofcode.spyglass.model.document.Document
import com.piratesofcode.spyglass.model.document.DocumentStatus
import com.piratesofcode.spyglass.model.user.User
import kotlinx.coroutines.launch

class DocumentsViewModel : ViewModel() {

    private var documentsLiveData: MutableLiveData<List<Document>> =
        MutableLiveData<List<Document>>()

    fun getDocumentsLiveData(): LiveData<List<Document>> {
        return documentsLiveData
    }

    fun getDocumentsByUser(user: User) {
        viewModelScope.launch {
            documentsLiveData.value = DocumentRepository.getDocumentsByUserId(user.id)
        }
    }

    fun getDocumentsByStatus(vararg status: DocumentStatus) {
        viewModelScope.launch {
            val temp = mutableListOf<Document>()
            status.forEach { status ->
                temp.addAll(DocumentRepository.getDocumentsByStatus(status))
            }
            documentsLiveData.value = temp
        }
    }
}