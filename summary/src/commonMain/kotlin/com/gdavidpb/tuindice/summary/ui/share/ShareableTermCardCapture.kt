package com.gdavidpb.tuindice.summary.ui.share

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import com.gdavidpb.tuindice.summary.domain.model.ShareBackground
import com.gdavidpb.tuindice.summary.presentation.model.ShareableTermCardItem

/**
 * Draws the term card and hands its bitmap to [onCaptured] once for each background.
 *
 * The card is drawn at its shared size and in its own density, so the bitmap is 1080 by 1620 pixels whatever the
 * screen it is drawn on. It is captured only after its artwork has finished loading: at once for the one the app ships
 * with, and when a remote artwork has loaded or has fallen back to the one the app ships with. The card is drawn
 * wherever it is placed, so the caller places it where the student does not see it, or where it is part of what is
 * shown.
 */
@Composable
fun ShareableTermCardCapture(
	item: ShareableTermCardItem,
	background: ShareBackground,
	onCaptured: (ImageBitmap) -> Unit
) {
	val graphicsLayer = rememberGraphicsLayer()
	var isArtReady by remember(background) { mutableStateOf(false) }

	CompositionLocalProvider(LocalDensity provides Density(density = SHARE_DENSITY, fontScale = 1f)) {
		ShareableTermCard(
			item = item,
			background = background,
			modifier = Modifier.recordInto(graphicsLayer),
			onArtReady = { isArtReady = true }
		)
	}

	LaunchedEffect(graphicsLayer, isArtReady) {
		if (isArtReady) {
			// The state change is composed in one frame, and the recording of the card is drawn in the next one.
			withFrameNanos { }
			withFrameNanos { }

			onCaptured(graphicsLayer.toImageBitmap())
		}
	}
}

// Three pixels per dp: the 360 by 540 dp card is 1080 by 1620 pixels.
internal const val SHARE_DENSITY = 3f

// Records what the card draws into the layer, and draws the layer where the card is, so the card is both seen and kept.
private fun Modifier.recordInto(graphicsLayer: GraphicsLayer): Modifier {
	return drawWithContent {
		graphicsLayer.record { this@drawWithContent.drawContent() }
		drawLayer(graphicsLayer)
	}
}
