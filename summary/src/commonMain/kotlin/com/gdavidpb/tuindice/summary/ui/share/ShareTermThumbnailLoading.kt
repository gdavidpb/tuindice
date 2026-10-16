package com.gdavidpb.tuindice.summary.ui.share

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.a11y_share_term_background_loading

// A picture that comes within this time, as one on disk does, changes the card without the mark ever showing.
internal const val SHARE_TERM_THUMBNAIL_LOADING_DELAY_MILLIS = 300L

// The thumbnail darkens under the indicator so that its white reads over any artwork.
private val ScrimColor = Color.Black.copy(alpha = 0.42f)

/**
 * The mark over the thumbnail of the chosen background while the card waits for its picture: the thumbnail darkens and
 * a small indicator turns on it, where the student tapped, and the card is left clean. It shows only once the wait has
 * lasted [SHARE_TERM_THUMBNAIL_LOADING_DELAY_MILLIS], so a picture that is already on the phone never flashes it, and
 * it goes as soon as the wait ends. The tile it sits in says it is loading too.
 */
@Composable
fun ShareTermThumbnailLoading(
	isLoading: Boolean,
	modifier: Modifier = Modifier
) {
	val isShown = remember { mutableStateOf(false) }
	val description = stringResource(Res.string.a11y_share_term_background_loading)

	LaunchedEffect(isLoading) {
		if (isLoading) delay(SHARE_TERM_THUMBNAIL_LOADING_DELAY_MILLIS)

		isShown.value = isLoading
	}

	// The wait has lasted long enough, and the mark goes in the same frame the wait ends, not one after.
	if (isLoading && isShown.value) {
		Box(
			modifier = modifier
				.fillMaxSize()
				.background(ScrimColor)
				.semantics { contentDescription = description },
			contentAlignment = Alignment.Center
		) {
			CircularProgressIndicator(
				modifier = Modifier.size(18.dp),
				color = Color.White,
				strokeWidth = 2.dp
			)
		}
	}
}
