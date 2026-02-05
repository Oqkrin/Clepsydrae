package oqk.ananke.clepsydrae.navigation

sealed class Screen(val route: String) {
    data object Clepsydra : Screen("clepsydra")
    data object Calendar : Screen("calendar")
    data object Statistics : Screen("statistics")
    data object Settings : Screen("settings")
}
