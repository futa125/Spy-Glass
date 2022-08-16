package com.piratesofcode.spyglass.model.user

data class Reviser(
    override val id: String,
    override val email: String,
    override val firstName: String,
    override val lastName: String,
    override val role: UserRole = UserRole.REVISER
) : User
