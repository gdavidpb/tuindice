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
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import com.gdavidpb.tuindice.base.ui.style.LocalTuIndiceAnimationsEnabled
import com.gdavidpb.tuindice.summary.domain.model.ShareBackground
import com.gdavidpb.tuindice.summary.presentation.model.ShareableTermCardItem

/**
 * Draws the term card into a picture and hands it to [onCaptured] once, when the artwork on screen is drawn: at the
 * shared size, 1080 by 1620 pixels whatever the screen it is composed on.
 *
 * The card is drawn into the picture only, never on the screen, so the caller places it where the student does not see
 * it. The picture is still: no animation runs in it, so it is the card of [item] over [background] at rest.
 */
@Composable
fun ShareableTermCardCapture(
	item: ShareableTermCardItem,
	background: ShareBackground,
	onCaptured: (ImageBitmap) -> Unit
) {
	val graphicsLayer = rememberGraphicsLayer()
	var isArtReady by remember(background) { mutableStateOf(false) }

	CompositionLocalProvider(
		LocalDensity provides Density(density = SHARE_DENSITY, fontScale = 1f),
		LocalTuIndiceAnimationsEnabled provides false
	) {
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

// Records what the card draws into the layer and draws nothing on the screen: the layer is the picture of the card.
private fun Modifier.recordInto(graphicsLayer: GraphicsLayer): Modifier {
	return drawWithContent {
		graphicsLayer.record { this@drawWithContent.drawContent() }
	}
}
