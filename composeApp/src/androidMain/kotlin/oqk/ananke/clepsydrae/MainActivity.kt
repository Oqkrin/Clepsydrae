    package oqk.ananke.clepsydrae

    import android.os.Bundle
    import androidx.activity.ComponentActivity
    import androidx.activity.compose.setContent
    import androidx.activity.enableEdgeToEdge
    import androidx.compose.ui.platform.LocalContext
    import oqk.ananke.clepsydrae.core.AndroidNotificationManager

    class MainActivity : ComponentActivity() {
        override fun onCreate(savedInstanceState: Bundle?) {
            enableEdgeToEdge()
            super.onCreate(savedInstanceState)
            setContent {
                ClepsydraeApp(notificationManager = AndroidNotificationManager(LocalContext.current))
            }
        }
    }
