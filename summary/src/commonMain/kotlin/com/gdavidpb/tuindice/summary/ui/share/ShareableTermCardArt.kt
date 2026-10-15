package com.gdavidpb.tuindice.summary.ui.share

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gdavidpb.tuindice.summary.presentation.model.ShareTermArtwork

/**
 * The artwork of the card, filling it: the one on screen, which fades in over the one before it. The card has no
 * artwork until its chosen one is known, and [onArtReady] and [onArtFailed] are handed on from the layer that draws the
 * picture.
 */
@Composable
fun ShareableTermCardArt(
	shown: ShareTermArtwork?,
	onArtReady: () -> Unit,
	onArtFailed: (ShareTermArtwork) -> Unit
) {
	ChangingCardContent(
		targetState = shown,
		modifier = Modifier.fillMaxSize(),
		label = "ShareableTermCardArtAnimatedContent",
		transitionSpec = { artTransform() }
	) { artwork ->
		if (artwork != null) {
			ShareableTermCardArtLayer(artwork = artwork, onArtReady = onArtReady, onArtFailed = onArtFailed)
		}
	}
}
