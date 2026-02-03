package oqk.ananke.clepsydrae

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import oqk.ananke.clepsydrae.clepsydrae.presentation.ClepsydraScreen
import oqk.ananke.clepsydrae.di.appModule
import org.koin.compose.KoinApplication
import org.koin.dsl.KoinConfiguration

@Composable
@Preview
fun App() {
    KoinApplication(KoinConfiguration { modules(appModule()) }) {
        ClepsydraScreen()
    }
}
