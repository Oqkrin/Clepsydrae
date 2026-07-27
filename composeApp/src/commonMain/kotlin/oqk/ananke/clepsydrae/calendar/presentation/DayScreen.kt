package oqk.ananke.clepsydrae.calendar.presentation

import androidx.compose.animation.expandIn
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.retain.retain
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import oqk.ananke.clepsydrae.navigation.Screen
import androidx.window.core.layout.WindowSizeClass
import oqk.ananke.clepsydrae.clepsydrae.presentation.ClepsydraCalendarBar
import oqk.ananke.clepsydrae.clepsydrae.presentation.ClepsydraScope
import oqk.ananke.clepsydrae.clepsydrae.presentation.ClepsydraScreenAction
import oqk.ananke.clepsydrae.clepsydrae.presentation.ClepsydraScreenViewModel
import oqk.ananke.clepsydrae.clepsydrae.presentation.ClepsydraSideEffect
import oqk.ananke.clepsydrae.clepsydrae.presentation.ClepsydraTimeBar
import oqk.ananke.clepsydrae.clepsydrae.presentation.MorphingTimer
import oqk.ananke.clepsydrae.clepsydrae.presentation.NotificationPermissionPopUp
import oqk.ananke.clepsydrae.clepsydrae.presentation.Rain
import oqk.ananke.clepsydrae.core.*
import oqk.ananke.clepsydrae.journal.presentation.ClepsydraJournal
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayScreen(navController: NavController) {
    val vw: ClepsydraScreenViewModel = koinViewModel()
    val st by vw.state.collectAsState()
    val onAction = vw::onAction

    val ws = LocalSizeInfo.current.sizeClass
    val sz = LocalSizeInfo.current.sizes
    val uiScale = LocalSettings.current.uiScale
    val isFirst = LocalSettings.current.isFirstClepsydra

    val notificationManager: NotificationManager = koinInject()
    LaunchedEffect(vw) {
        vw.effect.collect { effect ->
            when (effect) {
                is ClepsydraSideEffect.ShowPomodoroNotification -> {
                    notificationManager.sendPomodoroNotification(effect.clepsydra)
                }
            }
        }
    }

    val scope = retain(ws, sz, st) {
        object : ClepsydraScope {
            override val st = st
            override val onAction = onAction
            override val wsc: WindowSizeClass = ws
            override val sizes: DpSize = sz
            override val uiScale: Float = uiScale
            override val navController: NavController = navController
        }
    }

    with(scope) {
        Surface(Modifier.fillMaxSize()) {

            if (isFirst) {
                NotificationPermissionPopUp(st.showNotificationPermissionPopUp) {
                    onAction(ClepsydraScreenAction.OnFirstClepsydraCreationOnResult)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.navigationBars),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top App Bar
                if (!isShort) {
                    CenterAlignedTopAppBar(
                        modifier = Modifier.fillMaxWidth(),
                        title = { ClepsydraCalendarBar(Modifier) },
                        navigationIcon = {
                            SmallFloatingActionButton(
                                modifier = Modifier,
                                onClick = { onAction(ClepsydraScreenAction.ToggleHistory) }
                            ) {
                                Icon(Icons.AutoMirrored.Filled.List, "History")
                            }
                        },
                        actions = {
                            SmallFloatingActionButton(
                                modifier = Modifier,
                                onClick = { navController.navigate(Screen.SETTINGS.name) }
                            ) {
                                Icon(Icons.Default.Settings, "Settings")
                            }
                        }
                    )
                }

                // Main Content Area
                Box(Modifier.weight(1f)) {
                    Rain()

                    Box(
                        Modifier.fillMaxSize()
                            .padding(horizontal = if (isNarrow && !isShort) 4.dp else 0.dp, vertical = 16.dp)
                    ) {
                        st.coreClepsydra?.let {
                            MorphingTimer(Modifier.align(Alignment.TopCenter))
                        } ?: /*
                        ClepsydraInputFormV2(modifier = Modifier.align(Alignment.BottomCenter)
                            .padding(
                                start = 4.dp,
                                end = if(!isNarrow) 96.dp+4.dp else 4.dp,
                                bottom = if(isNarrow) 72.dp else 0.dp ))
                                */
                        //androidx.compose.animation.AnimatedVisibility
                        androidx.compose.animation.AnimatedVisibility(
                            st.showJournal,
                            modifier = Modifier.align(Alignment.Center),
                            enter = expandIn() + expandVertically(expandFrom = Alignment.Top),
                            exit = shrinkOut() + shrinkVertically(shrinkTowards = Alignment.Top),
                            label = "Journal"
                        ) {
                            ClepsydraJournal(Modifier.align(Alignment.Center))
                        }
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.navigationBars)
                            .padding(bottom = 16.dp, end = 16.dp).align(Alignment.BottomCenter),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (isShort) {
                            SmallFloatingActionButton(
                                modifier = Modifier
                                    .align(Alignment.End),
                                onClick = { navController.navigate(Screen.SETTINGS.name) }
                            ) {
                                Icon(Icons.Default.Settings, "Settings")
                            }
                            Spacer(Modifier.weight(1f))
                        }
                        //if (!isNarrow) ClepsydraNavigationBar(Modifier.align(Alignment.End).fillMaxHeight(iPhi))


                        Row(
                            modifier = Modifier.align(Alignment.End).heightIn(max = if(isShort) 48.dp else 56.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            //if (isNarrow && isShort) ClepsydraNavigationBar(modifier = Modifier.weight(1f))

                            ClepsydraTimeBar(modifier = Modifier.height(56.dp))
                        }

                        //if (isNarrow && !isShort) ClepsydraNavigationBar(modifier = Modifier.padding(start = 16.dp))
                    }
                }


            }
        }
    }
}
