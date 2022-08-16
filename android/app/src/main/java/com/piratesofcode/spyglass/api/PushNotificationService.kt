package com.piratesofcode.spyglass.api

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.graphics.Color
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.piratesofcode.spyglass.ui.auth.AuthenticationActivity
import com.piratesofcode.spyglass.utils.Constants
import com.piratesofcode.spyglass.utils.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import kotlin.math.absoluteValue


class PushNotificationService : FirebaseMessagingService() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        if (AuthenticationRepository.user != null) {
            NotificationRepository.storeTokenToDatabase(AuthenticationRepository.user!!.id)
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val data = remoteMessage.data
        var documentID: String? = null
        var notificationTitle: String? = null
        var notificationBody: String? = null

        if (data.isNotEmpty()) {
            val jsonData = JSONObject(data as Map<*, *>)
            try {
                documentID = jsonData.get("documentId") as String
                notificationTitle = jsonData.get("notificationTitle") as String
                notificationBody = jsonData.get("notificationBody") as String
            } catch (e: JSONException) {
                Log.d(
                    Constants.TAG,
                    "Incorrect information passed in notification data segment. Should be {documentId: <id>}"
                )
            }
        }

        val title: String = notificationTitle ?: "New document ready"
        val messageBody: String = notificationBody ?: "New document needs to be processed"

        if (documentID != null) {
            sendDocUpdateNotification(title, messageBody, documentID)
        }

    }

    private fun sendDocUpdateNotification(title: String, messageBody: String, documentId: String) {
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelID = Constants.NOTIFICATION_CHANNEL_ID

        val channelName: String = getString(com.piratesofcode.spyglass.R.string.channel_name)

        val importance = NotificationManager.IMPORTANCE_HIGH

        val notificationChannel = NotificationChannel(channelID, channelName, importance)

        val notificationID: Int = System.currentTimeMillis().toInt().absoluteValue

        val description = getString(com.piratesofcode.spyglass.R.string.channel_description)

        notificationChannel.description = description

        notificationChannel.enableLights(true)
        notificationChannel.lightColor = Color.BLUE

        notificationManager.createNotificationChannel(notificationChannel)

        scope.launch {
            val document = DocumentRepository.getDocumentByID(documentId)
            val messageBodyWithLabel = messageBody + "\nDocument label: ${document.label}"
            val intent = NotificationHelper.createNotificationClickIntent(
                applicationContext,
                document,
                AuthenticationActivity::class.java
            )

            val pendingIntent: PendingIntent = PendingIntent.getActivity(
                applicationContext, 0, intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val notificationBuilder: NotificationCompat.Builder =
                NotificationCompat.Builder(applicationContext, channelID)
                    .setContentTitle(title)
                    .setContentText(messageBodyWithLabel)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setSmallIcon(
                        resources.getIdentifier(
                            "ic_spyglass",
                            "drawable",
                            applicationContext.packageName
                        )
                    )
                    .setStyle(NotificationCompat.BigTextStyle().bigText(messageBodyWithLabel))
                    .setChannelId(channelID)

            notificationManager.notify(
                notificationID.toString(),
                notificationID,
                notificationBuilder.build()
            )
        }
    }
}
