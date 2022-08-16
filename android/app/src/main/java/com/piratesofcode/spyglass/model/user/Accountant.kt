package com.piratesofcode.spyglass.model.user

import com.piratesofcode.spyglass.model.document.DocumentType

data class Accountant(
    override val id: String,
    override val email: String,
    override val firstName: String,
    override val lastName: String,
    override val role: UserRole = UserRole.ACCOUNTANT,
    val specialization: DocumentType
) : User
