package oqk.ananke.clepsydrae.core

import androidx.compose.ui.window.Notification
import androidx.compose.ui.window.TrayState
import oqk.ananke.clepsydrae.clepsydrae.domain.Clepsydra
import oqk.ananke.clepsydrae.clepsydrae.domain.dts

class JvmNotificationManager(private val trayState: TrayState) : NotificationManager {

    override fun sendPomodoroNotification(currentClepsydra: Clepsydra) {
        // 1. Prepare dynamic content (Mirroring your Android Logic)
        val title = if (currentClepsydra.isActive) "🔥 Work Session Hit!" else "☕ Break Time Hit!"

        val body = buildString {
            append("${currentClepsydra.name ?: "Task"}: ")
            if (currentClepsydra.isActive) {
                append("Total Worked: ${dts(currentClepsydra.totalActiveTime)}")
            } else {
                append("Total Rested: ${dts(currentClepsydra.totalPassiveTime)}")
            }
            append("\nKeep the momentum going!")
        }

        // 2. Trigger the Desktop Toast/Notification
        trayState.sendNotification(
            Notification(
                title = title,
                message = body,
                type = Notification.Type.Info
            )
        )

        // Note: Desktop doesn't support custom vibration/colors
        // as deeply as Android, it uses the OS native styling.
    }
    override fun canNotify(): Boolean = true
}