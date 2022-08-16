package com.piratesofcode.spyglass.model.document

import java.io.Serializable

interface Document : Serializable {
    var id: String
    val timestamp: Long
    val label: String
    val documentStatus: DocumentStatus
    val documentByUser: DocumentByUser
    val type: DocumentType
}
