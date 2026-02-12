package oqk.ananke.clepsydrae.clepsydrae.presentation

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

@Composable
actual fun NotificationPermissionPopUp(shouldShowPopUp: Boolean, onResult: (Boolean) -> Unit) {
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        onResult(isGranted)
    }

    if(shouldShowPopUp) {
        LaunchedEffect(shouldShowPopUp) {
            if (shouldShowPopUp) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    launcher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    // Older Androids grant notification permission at install time
                    onResult(true)
                }
            }
        }
    }
}