package oqk.ananke.clepsydrae.core

import oqk.ananke.clepsydrae.clepsydrae.domain.Clepsydra

class JvmNotificationManager : NotificationManager {
    override fun sendPomodoroNotification(currentClepsydra: Clepsydra) {
    }

    override fun canNotify(): Boolean {
        return true
    }

    override fun askPermission() {
    }
}