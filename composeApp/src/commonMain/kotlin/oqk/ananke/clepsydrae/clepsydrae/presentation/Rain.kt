package oqk.ananke.clepsydrae.clepsydrae.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlinx.coroutines.delay
import oqk.ananke.clepsydrae.core.iPhi
import kotlin.collections.plus
import kotlin.math.sqrt
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun ClepsydraScope.Rain() {

    val timerActive = MaterialTheme.colorScheme.primary
    val timerInactive = MaterialTheme.colorScheme.secondary
    val color by remember(st.coreClepsydra?.isActive) {
        derivedStateOf { if (st.coreClepsydra?.isActive == true) timerActive else timerInactive }
    }
    var droplets by remember { mutableStateOf(listOf<Droplet>()) }
    val isActive = st.coreClepsydra?.isActive == true

    val rowPresets = getRowPresets()

    LaunchedEffect(isActive) {
        var offset = 0f
        while (true) {
            delay((if (isActive) Random.nextLong(40L, 100L) else Random.nextLong(200L, 400L)).milliseconds)
            val pattern = rowPresets.random()
            val baseSpeed = if (isActive) 0.028f else 0.012f

            offset = (offset + iPhi) % 1f
            val newDroplets = pattern.mapIndexed { idx, x ->
                val isStreak = Random.nextFloat() < if (isActive) 0.55f else 0.15f
                val sizeRandom = 1f - sqrt(Random.nextFloat())
                val spreadX = (x + offset + idx * 0.01f) % 1f

                Droplet(
                    progress = 0f,
                    x = (spreadX + Random.nextFloat() * 0.02f - 0.01f).coerceIn(0f, 1f),
                    speed = baseSpeed + Random.nextFloat() * 0.018f,
                    size = if (isStreak) 0.6f + sizeRandom * 0.5f else 0.4f + sizeRandom * 0.8f,
                    drift = if (isActive) 0.018f + Random.nextFloat() * 0.012f else Random.nextFloat() * 0.012f - 0.006f,
                    isStreak = isStreak
                )
            }
            droplets = droplets.filter { it.progress < 1f } + newDroplets
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(16.milliseconds)
            droplets = droplets.map { it.copy(progress = it.progress + it.speed) }.filter { it.progress < 1f }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawWithCache {
                onDrawBehind {
                    droplets.forEach { droplet ->
                        val eased = droplet.progress * droplet.progress
                        val alpha = (1f - eased) * if (isActive) 0.88f else 0.5f
                        val xDrift = droplet.drift * droplet.progress * size.width
                        val x = droplet.x * size.width + xDrift
                        val y = droplet.progress * size.height

                        if (droplet.isStreak) {
                            val streakLength = 18f * droplet.size
                            val path = Path().apply {
                                moveTo(x, y)
                                lineTo(x, y - streakLength)
                            }
                            drawPath(
                                path = path,
                                color = color.copy(alpha = alpha),
                                style = Stroke(width = 2.5f * droplet.size)
                            )
                        } else {
                            val w = 6f * droplet.size
                            val h = 9f * droplet.size
                            val path = Path().apply {
                                moveTo(x, y + h)
                                cubicTo(x + w * 0.35f, y + h * 0.7f, x + w * 0.45f, y + h * 0.4f, x, y)
                                cubicTo(x - w * 0.45f, y + h * 0.4f, x - w * 0.35f, y + h * 0.7f, x, y + h)
                                close()
                            }
                            drawPath(path = path, color = color.copy(alpha = alpha))
                        }
                    }
                }
            }
    )
}

@Composable
private fun getRowPresets(): List<List<Float>> = remember {
    listOf(
        (0..60).map { it / 60f },
        (0..55).map { it / 55f },
        (0..70).map { it / 70f },
        (0..45).map { it / 45f },
        (0..50).map { (it * 1.3f) % 1f },
        (0..48).map { (it * 1.7f) % 1f },
        (0..65).map { (it * 0.8f) % 1f },
        (0..42).map { (it * 2.1f) % 1f },
        (0..58).map { (it * 1.1f) % 1f },
        (0..52).map { (it * 1.5f) % 1f }
    )
}