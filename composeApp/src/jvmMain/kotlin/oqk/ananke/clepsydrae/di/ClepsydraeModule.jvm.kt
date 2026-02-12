package oqk.ananke.clepsydrae.di

import androidx.compose.ui.window.TrayState
import org.koin.dsl.module
import oqk.ananke.clepsydrae.clepsydrae.data.ClepsydraRepositoryImpl
import oqk.ananke.clepsydrae.clepsydrae.domain.ClepsydraRepository
import oqk.ananke.clepsydrae.clepsydrae.presentation.ClepsydraScreenViewModel
import oqk.ananke.clepsydrae.core.DriverFactory
import oqk.ananke.clepsydrae.core.JvmNotificationManager
import oqk.ananke.clepsydrae.core.NotificationManager
import oqk.ananke.clepsydrae.core.createDatabase
import oqk.ananke.clepsydrae.settings.data.SettingsRepositoryImpl
import oqk.ananke.clepsydrae.settings.domain.SettingsRepository
import oqk.ananke.clepsydrae.settings.presentation.SettingsScreenViewModel

actual fun platformModule(notificationManager: NotificationManager) = module {
    single { DriverFactory() }
    single { createDatabase(get()) }
    single<SettingsRepository> { SettingsRepositoryImpl(get()) }
    single<ClepsydraRepository> { ClepsydraRepositoryImpl(get()) }
    single<NotificationManager> { notificationManager }
    factory { ClepsydraScreenViewModel(
        clepsydraRepository = get(),
        settingsRepository = get(),
        notificationManager = get()
    ) }
    factory { SettingsScreenViewModel(get()) }
}
