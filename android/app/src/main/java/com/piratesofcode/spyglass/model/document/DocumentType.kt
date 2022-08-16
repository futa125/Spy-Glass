package com.piratesofcode.spyglass.model.document

import java.io.Serializable

enum class DocumentType(val value: String) : Serializable {
    RECEIPT("Receipt"),
    OFFER("Offer"),
    INTERNAL("Internal")
}
