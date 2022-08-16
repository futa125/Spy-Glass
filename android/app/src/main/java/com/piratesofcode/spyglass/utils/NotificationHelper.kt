package com.piratesofcode.spyglass.utils

import android.content.Context
import android.content.Intent
import com.google.firebase.messaging.FirebaseMessaging
import com.piratesofcode.spyglass.model.document.Document

object NotificationHelper {

    fun deleteToken() {
        FirebaseMessaging.getInstance().deleteToken()
    }

    fun <T> createNotificationClickIntent(
        context: Context,
        document: Document,
        activityClass: Class<T>
    ): Intent {
        val intent = Intent(context, activityClass)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.putExtra(Constants.EXTRA_ACTIVATED_BY_NOTIFICATION, true)
        intent.putExtra(Constants.EXTRA_DOCUMENT, document)

        return intent
    }
}