package com.piratesofcode.spyglass.model.document

import java.io.Serializable

data class InternalDocument(
    override var id: String,
    override val timestamp: Long,
    override val label: String,
    override val documentStatus: DocumentStatus,
    override val documentByUser: DocumentByUser,
    override val type: DocumentType = DocumentType.INTERNAL,
    val content: String,
) : Document, Serializable
