package com.piratesofcode.spyglass.model.document

import java.io.Serializable

data class DocumentStatus(
    var archived: Boolean = false,
    var signed: Boolean = false,
    var toBeSigned: Boolean = false,
    var revised: Boolean = false,
    var scannedProperly: Boolean = false
) : Serializable {

    override fun toString() =
        when {
            archived -> "Archived"
            signed -> "Signed"
            toBeSigned -> "To Be Signed"
            revised -> "Revised"
            scannedProperly -> "Pending"
            else -> "Not Approved"
        }
}
