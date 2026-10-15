package com.gdavidpb.tuindice.summary.ui.share

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.abs

// How far a finger has to travel across the preview before it means the next background, as a photo filter does.
private val SwipeThreshold = 48.dp

/**
 * Swiping across the preview moves the card to the background beside the chosen one, as the strip under it would: to
 * [nextId] for a swipe to the left, and to [previousId] for a swipe to the right. Both come from the sheet's state,
 * where they are null at either end of the strip and when nothing is chosen, so a swipe past an end leads nowhere.
 */
internal fun Modifier.swipeBetweenBackgrounds(
	previousId: String?,
	nextId: String?,
	onBackgroundClick: (backgroundId: String) -> Unit
): Modifier = pointerInput(previousId, nextId) {
	var travelled = 0f

	detectHorizontalDragGestures(
		onDragStart = { travelled = 0f },
		onDragEnd = {
			if (abs(travelled) >= SwipeThreshold.toPx()) {
				val target = if (travelled < 0f) nextId else previousId

				target?.let(onBackgroundClick)
			}
		},
		onHorizontalDrag = { change, amount ->
			change.consume()
			travelled += amount
		}
	)
}
