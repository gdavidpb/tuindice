package com.gdavidpb.tuindice.summary.ui.share

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.summary.presentation.model.ShareTermBackgroundItem
import kotlin.math.abs

// How far a finger has to travel across the preview before it means the next background, as a photo filter does.
private val SwipeThreshold = 48.dp

/**
 * The background beside the chosen one in the strip: the next one for a swipe to the left ([towardsNext]), the
 * previous one for a swipe to the right. Null at either end of the strip, or when nothing is chosen.
 */
internal fun neighbourBackgroundId(backgrounds: List<ShareTermBackgroundItem>, towardsNext: Boolean): String? {
	val chosen = backgrounds.indexOfFirst { item -> item.selected }

	if (chosen < 0) return null

	return backgrounds.getOrNull(if (towardsNext) chosen + 1 else chosen - 1)?.background?.id
}

/**
 * Swiping across the preview moves the card to the background beside the chosen one, as the strip under it would.
 */
internal fun Modifier.swipeBetweenBackgrounds(
	backgrounds: List<ShareTermBackgroundItem>,
	onBackgroundClick: (backgroundId: String) -> Unit
): Modifier = pointerInput(backgrounds) {
	var travelled = 0f

	detectHorizontalDragGestures(
		onDragStart = { travelled = 0f },
		onDragEnd = {
			if (abs(travelled) >= SwipeThreshold.toPx()) {
				neighbourBackgroundId(backgrounds = backgrounds, towardsNext = travelled < 0f)?.let(onBackgroundClick)
			}
		},
		onHorizontalDrag = { change, amount ->
			change.consume()
			travelled += amount
		}
	)
}
