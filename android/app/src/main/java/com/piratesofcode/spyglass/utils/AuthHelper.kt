package com.piratesofcode.spyglass.utils

import com.google.firebase.firestore.DocumentSnapshot
import com.piratesofcode.spyglass.model.document.DocumentType
import com.piratesofcode.spyglass.model.user.*

object AuthHelper {
    @JvmStatic
    fun createUser(
        userId: String,
        email: String,
        firstName: String,
        lastName: String,
        role: String,
        specialization: String?
    ) = when (role) {
        UserRole.EMPLOYEE.value -> Employee(userId, email, firstName, lastName)
        UserRole.REVISER.value -> Reviser(userId, email, firstName, lastName)
        UserRole.ACCOUNTANT.value -> Accountant(
            userId, email, firstName, lastName,
            specialization = when (specialization) {
                DocumentType.RECEIPT.value -> DocumentType.RECEIPT
                DocumentType.OFFER.value -> DocumentType.OFFER
                DocumentType.INTERNAL.value -> DocumentType.INTERNAL
                else -> throw IllegalStateException("Invalid specialization")
            }
        )
        UserRole.DIRECTOR.value -> Director(userId, email, firstName, lastName)
        else -> throw IllegalStateException("Invalid role")
    }

    @JvmStatic
    fun createUserFromSnapshot(snapshot: DocumentSnapshot) =
        when (snapshot["role"]) {
            UserRole.EMPLOYEE.value.lowercase() -> Employee(
                snapshot.id, snapshot["email"].toString(),
                snapshot["firstName"].toString(), snapshot["lastName"].toString()
            )
            UserRole.REVISER.value.lowercase() -> Reviser(
                snapshot.id, snapshot["email"].toString(),
                snapshot["firstName"].toString(), snapshot["lastName"].toString()
            )
            UserRole.ACCOUNTANT.value.lowercase() -> Accountant(
                snapshot.id, snapshot["email"].toString(),
                snapshot["firstName"].toString(), snapshot["lastName"].toString(),
                specialization = getSpecializationFromSnapshot(snapshot)
            )
            UserRole.DIRECTOR.value.lowercase() -> Director(
                snapshot.id, snapshot["email"].toString(),
                snapshot["firstName"].toString(), snapshot["lastName"].toString()
            )
            else -> throw IllegalStateException("Invalid role")
        }

    @JvmStatic
    private fun getSpecializationFromSnapshot(snapshot: DocumentSnapshot) =
        when (snapshot["specialization"]) {
            DocumentType.RECEIPT.value.lowercase() -> DocumentType.RECEIPT
            DocumentType.OFFER.value.lowercase() -> DocumentType.OFFER
            DocumentType.INTERNAL.value.lowercase() -> DocumentType.INTERNAL
            else -> throw IllegalStateException("Invalid specialization")
        }

    @JvmStatic
    fun createMapFromUser(user: User) =
        if (user.role == UserRole.ACCOUNTANT) {
            user as Accountant
            hashMapOf(
                "email" to user.email,
                "firstName" to user.firstName,
                "lastName" to user.lastName,
                "role" to user.role.value.lowercase(),
                "specialization" to user.specialization.value.lowercase()
            )
        } else {
            hashMapOf(
                "email" to user.email,
                "firstName" to user.firstName,
                "lastName" to user.lastName,
                "role" to user.role.value.lowercase()
            )
        }
}