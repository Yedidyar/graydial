package com.graydial.android.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.graydial.android.domain.ColorMath
import com.graydial.android.domain.GrayscaleIntensity

private const val STOPS = 25

/** Hue spectrum before and after applying exactly the matrix the system uses. */
@Composable
fun SpectrumPreview(intensity: Int, modifier: Modifier = Modifier) {
    val saturation = GrayscaleIntensity.coerce(intensity).saturation
    val original = remember { spectrum(100) }
    val transformed = remember(saturation) { spectrum(saturation) }
    Column(
        modifier.semantics { contentDescription = "Color preview at $intensity percent grayscale" },
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Label("ORIGINAL")
        Bar(original)
        Label("AT $intensity%")
        Bar(transformed)
    }
}

@Composable
private fun Label(text: String) {
    Text(text, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun Bar(colors: List<Color>) {
    Canvas(Modifier.fillMaxWidth().height(20.dp)) {
        drawRoundRect(
            brush = Brush.horizontalGradient(colors),
            cornerRadius = CornerRadius(size.height / 2, size.height / 2),
        )
    }
}

private fun spectrum(saturation: Int): List<Color> = (0 until STOPS).map { i ->
    val rgb = ColorMath.hue(360f * i / (STOPS - 1))
    val out = ColorMath.desaturate(rgb[0], rgb[1], rgb[2], saturation)
    Color(out[0], out[1], out[2])
}
