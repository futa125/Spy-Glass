package com.piratesofcode.spyglass.api

import android.util.Log
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging

object NotificationRepository {
    private val tokens = Firebase.firestore.collection("FCMTokens")

    fun storeTokenToDatabase(userID: String) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                val tokenKV = hashMapOf<String, String>("FCMToken" to token)
                tokens.document(userID).set(tokenKV).addOnSuccessListener {
                    Log.d("FCMToken", "Token successfully stored")
                }
            } else {
                Log.d("FCMToken", "There was an error while storing token to database")
            }
        }
    }
}
