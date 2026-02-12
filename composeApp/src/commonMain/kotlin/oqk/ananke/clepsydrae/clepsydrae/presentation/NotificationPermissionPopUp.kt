package oqk.ananke.clepsydrae.clepsydrae.presentation

import androidx.compose.runtime.Composable

@Composable
expect fun NotificationPermissionPopUp(shouldShowPopUp: Boolean, onResult: (Boolean) -> Unit)