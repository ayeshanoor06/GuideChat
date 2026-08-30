
package com.ayesha.guidechat.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.ayesha.guidechat.MainActivity
import com.ayesha.guidechat.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService :
    FirebaseMessagingService() {

    companion object {

        const val CHANNEL_ID =
            "guidechat_messages"

        private const val CHANNEL_NAME =
            "GuideChat Messages"

        private const val CHANNEL_DESCRIPTION =
            "Notifications for new GuideChat messages"

        private const val NOTIFICATION_ID =
            1001
    }

    override fun onCreate() {
        super.onCreate()

        createNotificationChannel()
    }

    // =========================================================
    // RECEIVE FCM MESSAGE
    // =========================================================

    override fun onMessageReceived(
        remoteMessage: RemoteMessage
    ) {
        super.onMessageReceived(remoteMessage)

        val title =
            remoteMessage.notification?.title
                ?: remoteMessage.data["title"]
                ?: "GuideChat"

        val body =
            remoteMessage.notification?.body
                ?: remoteMessage.data["body"]
                ?: "You have a new message"

        showNotification(
            title = title,
            body = body,
            data = remoteMessage.data
        )
    }

    // =========================================================
    // TOKEN REFRESH
    // =========================================================

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        /*
         * The token may change.
         *
         * NotificationRepository will save the new token
         * to the currently logged-in user's Firestore document.
         */

        try {

            val notificationRepository =
                com.ayesha.guidechat.data.NotificationRepository()

            notificationRepository.updateToken(
                token = token
            )

        } catch (exception: Exception) {

            /*
             * The user might not be logged in yet.
             *
             * In that case there is nothing to update.
             */
        }
    }

    // =========================================================
    // CREATE NOTIFICATION CHANNEL
    // =========================================================

    private fun createNotificationChannel() {

        if (Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.O
        ) {

            val channel =
                NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {

                    description =
                        CHANNEL_DESCRIPTION

                    enableVibration(true)

                    setShowBadge(true)
                }

            val notificationManager =
                getSystemService(
                    Context.NOTIFICATION_SERVICE
                ) as NotificationManager

            notificationManager.createNotificationChannel(
                channel
            )
        }
    }

    // =========================================================
    // SHOW NOTIFICATION
    // =========================================================

    private fun showNotification(
        title: String,
        body: String,
        data: Map<String, String>
    ) {

        createNotificationChannel()

        val intent =
            Intent(
                this,
                MainActivity::class.java
            ).apply {

                flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TOP

                data.forEach { (key, value) ->

                    putExtra(
                        key,
                        value
                    )
                }
            }

        val pendingIntent =
            PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
            )

        val builder =
            NotificationCompat.Builder(
                this,
                CHANNEL_ID
            )
                .setSmallIcon(
                    R.drawable.ic_launcher_foreground
                )
                .setContentTitle(
                    title
                )
                .setContentText(
                    body
                )
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(body)
                )
                .setPriority(
                    NotificationCompat.PRIORITY_HIGH
                )
                .setAutoCancel(true)
                .setVibrate(
                    longArrayOf(
                        0,
                        250,
                        150,
                        250
                    )
                )
                .setContentIntent(
                    pendingIntent
                )

        try {

            NotificationManagerCompat
                .from(this)
                .notify(
                    NOTIFICATION_ID,
                    builder.build()
                )

        } catch (exception: SecurityException) {

            /*
             * Notification permission was not granted.
             *
             * Android 13+ requires POST_NOTIFICATIONS.
             */
        }
    }
}