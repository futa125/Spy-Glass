package com.piratesofcode.spyglass.model.user

interface User {
    val id: String
    val email: String
    val firstName: String
    val lastName: String
    val role: UserRole
}
