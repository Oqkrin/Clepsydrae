package oqk.ananke.clepsydrae.core

import oqk.ananke.clepsydrae.clepsydrae.domain.Clepsydra

interface NotificationManager {
    fun sendPomodoroNotification(currentClepsydra: Clepsydra)
    fun canNotify(): Boolean
}