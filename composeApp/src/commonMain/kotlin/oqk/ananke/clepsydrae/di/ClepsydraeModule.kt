package oqk.ananke.clepsydrae.di

import org.koin.dsl.module

expect fun platformModule(): org.koin.core.module.Module

fun clepsydraeModule() = module {
    includes(platformModule())
}
