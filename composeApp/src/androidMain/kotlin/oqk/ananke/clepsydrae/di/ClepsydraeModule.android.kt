package oqk.ananke.clepsydrae.di

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import oqk.ananke.clepsydrae.clepsydrae.data.ClepsydraRepositoryImpl
import oqk.ananke.clepsydrae.clepsydrae.domain.ClepsydraRepository
import oqk.ananke.clepsydrae.clepsydrae.presentation.ClepsydraScreenViewModel
import oqk.ananke.clepsydrae.core.AndroidNotificationManager
import oqk.ananke.clepsydrae.core.DriverFactory
import oqk.ananke.clepsydrae.core.NotificationManager
import oqk.ananke.clepsydrae.core.createDatabase
import oqk.ananke.clepsydrae.settings.data.SettingsRepositoryImpl
import oqk.ananke.clepsydrae.settings.domain.SettingsRepository
import oqk.ananke.clepsydrae.settings.presentation.SettingsScreenViewModel

actual fun platformModule() = module {
    single { DriverFactory(androidContext()) }
    single { createDatabase(get()) }
    single<SettingsRepository> { SettingsRepositoryImpl(get()) }
    single<ClepsydraRepository> { ClepsydraRepositoryImpl(get()) }
    single<NotificationManager> { AndroidNotificationManager(androidContext()) }
    factory { ClepsydraScreenViewModel(get()) }
    factory { SettingsScreenViewModel(get()) }
}
