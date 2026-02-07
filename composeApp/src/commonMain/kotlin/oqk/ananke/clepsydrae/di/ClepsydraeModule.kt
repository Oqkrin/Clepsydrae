package oqk.ananke.clepsydrae.di

import androidx.compose.runtime.State
import androidx.window.core.layout.WindowSizeClass
import org.koin.dsl.module

expect fun platformModule(): org.koin.core.module.Module

fun clepsydraeModule(windowSizeClass: State<WindowSizeClass>) = module {
    single { windowSizeClass }
    includes(platformModule())
}
