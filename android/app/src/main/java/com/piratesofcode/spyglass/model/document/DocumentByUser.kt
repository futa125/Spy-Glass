package com.piratesofcode.spyglass.model.document;

import java.io.Serializable;

data class DocumentByUser(
    var archivedBy: String = "",
    var revisedBy: String = "",
    var sentToSignBy: String = "",
    var signedBy: String = ""
) : Serializable
