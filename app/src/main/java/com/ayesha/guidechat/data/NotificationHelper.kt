package com.ayesha.guidechat.data

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.ayesha.guidechat.MainActivity

class NotificationHelper(
    private val context: Context
) {

    companion object {

        private const val CHANNEL_ID =
            "guidechat_messages"

        private const val CHANNEL_NAME =
            "GuideChat Messages"

        private const val CHANNEL_DESCRIPTION =
            "Notifications for new GuideChat messages"

        private const val BASE_NOTIFICATION_ID =
            5000
    }

    init {
        createNotificationChannel()
    }

    // =========================================================
    // CREATE NOTIFICATION CHANNEL
    // =========================================================

    private fun createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channel =
                NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {

                    description =
                        CHANNEL_DESCRIPTION

                    enableVibration(true)
                }

            val notificationManager =
                context.getSystemService(
                    Context.NOTIFICATION_SERVICE
                ) as NotificationManager

            notificationManager.createNotificationChannel(
                channel
            )
        }
    }

    // =========================================================
    // SHOW MESSAGE NOTIFICATION
    // =========================================================

    fun showMessageNotification(
        senderName: String,
        message: String,
        notificationKey: String = senderName
    ) {

        // -----------------------------------------------------
        // Android 13+ notification permission
        // -----------------------------------------------------

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.TIRAMISU
        ) {

            if (
                context.checkSelfPermission(
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {

                return
            }
        }

        // -----------------------------------------------------
        // Open the app when notification is tapped
        // -----------------------------------------------------

        val intent =
            Intent(
                context,
                MainActivity::class.java
            ).apply {

                flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TOP
            }

        val pendingIntent =
            PendingIntent.getActivity(
                context,
                notificationKey.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
            )

        // -----------------------------------------------------
        // Notification
        // -----------------------------------------------------

        val notification =
            NotificationCompat.Builder(
                context,
                CHANNEL_ID
            )
                .setSmallIcon(
                    android.R.drawable.ic_dialog_info
                )
                .setContentTitle(
                    senderName
                )
                .setContentText(
                    message
                )
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(message)
                )
                .setPriority(
                    NotificationCompat.PRIORITY_HIGH
                )
                .setAutoCancel(true)
                .setContentIntent(
                    pendingIntent
                )
                .setVibrate(
                    longArrayOf(
                        0,
                        300,
                        200,
                        300
                    )
                )
                .build()

        // -----------------------------------------------------
        // Unique notification ID
        // -----------------------------------------------------

        val notificationId =
            BASE_NOTIFICATION_ID +
                    kotlin.math.abs(
                        notificationKey.hashCode()
                    ) % 100000

        NotificationManagerCompat
            .from(context)
            .notify(
                notificationId,
                notification
            )
    }
}