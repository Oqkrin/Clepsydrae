package oqk.ananke.clepsydrae.clepsydrae.presentation

import androidx.compose.runtime.Composable

@Composable
actual fun NotificationPermissionPopUp(shouldShowPopUp: Boolean, onResult: (Boolean) -> Unit) {
    onResult(true)
}