package com.piratesofcode.spyglass.api

import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.piratesofcode.spyglass.model.user.User
import com.piratesofcode.spyglass.utils.AuthHelper
import kotlinx.coroutines.tasks.await

object AuthenticationRepository {

    var user: User? = null
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val users = Firebase.firestore.collection("users")

    suspend fun login(email: String, password: String): AuthResult =
        firebaseAuth.signInWithEmailAndPassword(email, password).await()

    suspend fun register(email: String, password: String): AuthResult =
        firebaseAuth.createUserWithEmailAndPassword(email, password).await()

    suspend fun getUserFromFirestore(userId: String) =
        AuthHelper.createUserFromSnapshot(users.document(userId).get().await())

    suspend fun saveUserToFirestore(user: User): Void? =
        users.document(user.id).set(AuthHelper.createMapFromUser(user)).await()

    fun getCurrentFirebaseUserId() = firebaseAuth.currentUser!!.uid

    fun getCurrentFirebaseUser() = firebaseAuth.currentUser

    fun logout() = firebaseAuth.signOut()
}
