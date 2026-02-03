package oqk.ananke.clepsydrae.di

import org.koin.dsl.module
import oqk.ananke.clepsydrae.clepsydrae.data.ClepsydraRepositoryImpl
import oqk.ananke.clepsydrae.clepsydrae.domain.ClepsydraRepository
import oqk.ananke.clepsydrae.clepsydrae.presentation.ClepsydraViewModel
import oqk.ananke.clepsydrae.core.DriverFactory
import oqk.ananke.clepsydrae.core.createDatabase

actual fun platformModule() = module {
    single { DriverFactory() }
    single { createDatabase(get()) }
    single<ClepsydraRepository> { ClepsydraRepositoryImpl(get()) }
    factory { ClepsydraViewModel(get()) }
}
