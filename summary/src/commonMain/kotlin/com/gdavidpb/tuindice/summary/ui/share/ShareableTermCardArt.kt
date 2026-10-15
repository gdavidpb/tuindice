package com.gdavidpb.tuindice.summary.ui.share

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import com.gdavidpb.tuindice.summary.presentation.model.ShareTermArtwork

private const val TOP_FADE_FRACTION = 0.52f
private const val BOTTOM_FADE_FRACTION = 0.24f

// Where each fade changes its slope, between its edge and the edge of the card. The top fade holds most of its opacity
// down to the labels of the figures, so they read at 4.5:1 or more over every artwork, and only then gives way to it.
private const val TOP_FADE_MIDDLE = 0.70f
private const val BOTTOM_FADE_MIDDLE = 0.5f

/**
 * The artwork of the card, filling it, with the two fades of bone over its top and its foot so that the text reads over
 * any picture. The card has no artwork until its chosen one is known, and [onArtReady] and [onArtFailed] are handed on
 * from the layer that draws the picture. The card changes its artwork as a whole, so this part does not fade by itself.
 */
@Composable
fun ShareableTermCardArt(
	shown: ShareTermArtwork?,
	onArtReady: () -> Unit,
	onArtFailed: (ShareTermArtwork) -> Unit
) {
	Box(modifier = Modifier.fillMaxSize()) {
		if (shown != null) {
			ShareableTermCardArtLayer(artwork = shown, onArtReady = onArtReady, onArtFailed = onArtFailed)
		}

		Box(
			modifier = Modifier
				.align(Alignment.TopCenter)
				.fillMaxWidth()
				.fillMaxHeight(TOP_FADE_FRACTION)
				.background(
					Brush.verticalGradient(
						0f to CardBone.copy(alpha = 0.9f),
						TOP_FADE_MIDDLE to CardBone.copy(alpha = 0.78f),
						1f to CardBone.copy(alpha = 0f)
					)
				)
		)

		Box(
			modifier = Modifier
				.align(Alignment.BottomCenter)
				.fillMaxWidth()
				.fillMaxHeight(BOTTOM_FADE_FRACTION)
				.background(
					Brush.verticalGradient(
						0f to CardBone.copy(alpha = 0f),
						BOTTOM_FADE_MIDDLE to CardBone.copy(alpha = 0.7f),
						1f to CardBone.copy(alpha = 0.92f)
					)
				)
		)
	}
}
