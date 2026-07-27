package oqk.ananke.clepsydrae.clepsydrae.presentation.old

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.HourglassFull
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.HourglassDisabled
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemColors
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import androidx.window.core.layout.WindowSizeClass
import oqk.ananke.clepsydrae.clepsydrae.presentation.ClepsydraScope
import oqk.ananke.clepsydrae.clepsydrae.presentation.ClepsydraScreenAction
import oqk.ananke.clepsydrae.core.iPhi

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private fun ClepsydraScope.ClepsydraNavigationBar(modifier: Modifier = Modifier.Companion) {
    val items = listOf("Journal", "Clepsydra", "Habits")
    val selectedIcons = listOf(Icons.Filled.Book, Icons.Filled.HourglassFull, Icons.Filled.Star)
    val unselectedIcons = listOf(Icons.Outlined.Book, Icons.Outlined.HourglassDisabled, Icons.Outlined.StarBorder)
    var selectedItem by remember { mutableIntStateOf(1) }
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(st.showJournal) {
        if (st.showJournal) selectedItem = 0
    }

    val itemColors = NavigationBarItemColors(
        selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
        unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(iPhi),
        selectedTextColor = MaterialTheme.colorScheme.primary,
        unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(iPhi),
        selectedIndicatorColor = MaterialTheme.colorScheme.primaryContainer,
        disabledIconColor = MaterialTheme.colorScheme.error,
        disabledTextColor = MaterialTheme.colorScheme.error
    )

    Card(
        shape = RoundedCornerShape(40),
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        if (isNarrow) {
            NavigationBar(
                containerColor = NavigationBarDefaults.containerColor.copy(iPhi),
                modifier = Modifier.height(64.dp).widthIn(max = WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND.dp * 2 / 3)
            ) {
                items.fastForEachIndexed { i, label ->
                    NavigationBarItem(
                        selected = i == selectedItem,
                        label = { Text(label, style = MaterialTheme.typography.labelMediumEmphasized, maxLines = 1) },
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                            selectedItem = i
                            onAction(ClepsydraScreenAction.OnToggleShowJournal(i == 0))
                        },
                        icon = {
                            Icon(
                                if (i == selectedItem) selectedIcons[i] else unselectedIcons[i],
                                label,
                                modifier = Modifier.size(if (isShort) 14.dp else 24.dp)
                            )
                        },
                        colors = itemColors,
                        alwaysShowLabel = !(isNarrow && isShort)
                    )
                }
            }
        } else {
            NavigationRail(
                containerColor = NavigationBarDefaults.containerColor.copy(iPhi),
                modifier = Modifier.width(64.dp)
            ) {
                Column(
                    Modifier.fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceAround
                ) {
                    items.fastForEachIndexed { i, label ->
                        NavigationRailItem(
                            modifier = Modifier.weight(1f),
                            selected = i == selectedItem,
                            label = {
                                Text(
                                    label,
                                    style = MaterialTheme.typography.labelSmallEmphasized,
                                    maxLines = 1
                                )
                            },
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                                selectedItem = i
                                onAction(ClepsydraScreenAction.OnToggleShowJournal(i == 0))
                            },
                            icon = {
                                Icon(
                                    if (i == selectedItem) selectedIcons[i] else unselectedIcons[i],
                                    label,
                                    Modifier.size(24.dp)
                                )
                            },
                            colors = NavigationRailItemColors(
                                selectedIconColor = itemColors.selectedIconColor,
                                unselectedIconColor = itemColors.unselectedIconColor,
                                selectedTextColor = itemColors.selectedTextColor,
                                unselectedTextColor = itemColors.unselectedTextColor,
                                selectedIndicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(iPhi),
                                disabledIconColor = itemColors.disabledIconColor,
                                disabledTextColor = itemColors.disabledTextColor
                            ),
                            alwaysShowLabel = !isShort
                        )
                    }
                }
            }
        }
    }
}