package oqk.ananke.clepsydrae.core

import android.app.NotificationChannel
import android.app.PendingIntent
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import oqk.ananke.clepsydrae.clepsydrae.domain.Clepsydra
import oqk.ananke.clepsydrae.clepsydrae.domain.dts

class AndroidNotificationManager(val context: Context): NotificationManager {

    private val nm: android.app.NotificationManager = context.getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
    private val notificationChannels = listOf("pomodoro")

    init {
        notificationChannels.forEach {
            createNotificationChannel(it)
        }
    }

    private fun createNotificationChannel(channel: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm.createNotificationChannel(
                NotificationChannel(
                    channel,
                    channel,
                    android.app.NotificationManager.IMPORTANCE_HIGH
                )
            )
        }
    }

    override fun sendPomodoroNotification(currentClepsydra: Clepsydra) {
        val channelId = "pomodoro"

        // 1. Prepare dynamic content based on state
        val title = if (currentClepsydra.isActive) "🔥 Work Session Hit!" else "☕ Break Time Hit!"
        val sessionStats = if (currentClepsydra.isActive) {
            "Total Worked: ${dts(currentClepsydra.totalActiveTime)}"
        } else {
            "Total Rested: ${dts(currentClepsydra.totalPassiveTime)}"
        }

        // 2. Intent to open the app
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 3. Build the "Cool" Notification
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Replace with your app logo
            .setContentTitle(title)
            .setContentText("${currentClepsydra.name ?: "Task"}: $sessionStats")
            .setSubText("Clepsydrae Tracker") // Small text at the top
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            // Make it pop: Use a BigTextStyle for more info
            .setStyle(NotificationCompat.BigTextStyle().bigText(
                "Target reached for ${currentClepsydra.name ?: "your session"}.\n" +
                        "You've been ${if (currentClepsydra.isActive) "grinding" else "relaxing"} for ${dts(currentClepsydra.lastStateChange.elapsedNow())}.\n" +
                        "Keep the momentum going!"
            ))
            // Color the notification (Matches your app's primary color)
            .setColor(0xFF6200EE.toInt())
            .setColorized(true)
            // Add a "quick action" button directly in the notification
            .addAction(android.R.drawable.ic_menu_revert, "Open App", pendingIntent)
            // Vibration pattern: 0ms delay, 250ms on, 250ms off, 250ms on
            .setVibrate(longArrayOf(0, 250, 250, 250))

        with(NotificationManagerCompat.from(context)) {
            if(areNotificationsEnabled()) notify(1001, builder.build())
        }
    }

    override fun canNotify(): Boolean {
        return nm.areNotificationsEnabled()
    }

}