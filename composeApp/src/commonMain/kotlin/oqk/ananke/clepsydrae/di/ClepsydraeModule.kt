package oqk.ananke.clepsydrae.di

import oqk.ananke.clepsydrae.core.NotificationManager
import org.koin.dsl.module

typealias isMobile = Boolean
expect fun platformModule(notificationManager: NotificationManager): org.koin.core.module.Module

fun clepsydraeModule(notificationManager: NotificationManager) = module {
    includes(platformModule(notificationManager))
}
