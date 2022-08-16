package com.piratesofcode.spyglass.model.document

import java.io.Serializable

data class Offer(
    override var id: String,
    override val timestamp: Long,
    override val label: String,
    override val documentStatus: DocumentStatus,
    override val documentByUser: DocumentByUser,
    override val type: DocumentType = DocumentType.OFFER,
    var items: Map<String, Double>,
    var totalPrice: Double
) : Document, Serializable
