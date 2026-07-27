package oqk.ananke.clepsydrae.calendar.presentation

import androidx.compose.ui.graphics.Path
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

internal fun createPolygonPath(cx: Float, cy: Float, radius: Float, sides: Int): Path {
    val path = Path()
    val angleOffset = -PI / 2
    for (i in 0 until sides) {
        val angle = i * (2 * PI / sides) + angleOffset
        val x = cx + radius * cos(angle).toFloat()
        val y = cy + radius * sin(angle).toFloat()
        if (i == 0) {
            path.moveTo(x, y)
        } else {
            path.lineTo(x, y)
        }
    }
    path.close()
    return path
}
