package oqk.ananke.clepsydrae

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.currentWindowDpSize
import androidx.compose.runtime.*
import oqk.ananke.clepsydrae.core.ClepsydraeTheme
import oqk.ananke.clepsydrae.core.LocalSettings
import oqk.ananke.clepsydrae.core.LocalSizeInfo
import oqk.ananke.clepsydrae.core.NotificationManager
import oqk.ananke.clepsydrae.core.SizeInfo
import oqk.ananke.clepsydrae.di.clepsydraeModule
import oqk.ananke.clepsydrae.navigation.ClepsydraeNavigation
import oqk.ananke.clepsydrae.settings.domain.Settings
import oqk.ananke.clepsydrae.settings.domain.SettingsRepository
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.dsl.koinConfiguration

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun ClepsydraeApp(
    notificationManager: NotificationManager,
    clepsydraeScreenContent: @Composable () -> Unit = { ClepsydraeNavigation() }
) {
    // Pass windowSizeClass to Koin if your module really needs it dynamically,
    // otherwise prefer passing it via CompositionLocal or to specific Composables.


    KoinApplication(
        configuration = koinConfiguration(declaration = { modules(clepsydraeModule(notificationManager)) })
    ) {
        val repository: SettingsRepository = koinInject()
        val settings by repository.getSettings().collectAsState(Settings())

        val windowAdaptiveInfo = currentWindowAdaptiveInfo()
        val sizes = currentWindowDpSize()
        val currentSizeInfo =
            remember(windowAdaptiveInfo, sizes) { SizeInfo(windowAdaptiveInfo.windowSizeClass, sizes) }

        CompositionLocalProvider(LocalSettings provides settings, LocalSizeInfo provides currentSizeInfo) {
            ClepsydraeTheme(
                isDarkTheme = settings.isDarkTheme,
                themeName = settings.theme,
                fontSize = settings.fontSize
            ) {
                clepsydraeScreenContent()
            }
        }
    }
}
