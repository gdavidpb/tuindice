package com.gdavidpb.tuindice.ui.navigation

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.abs

fun Modifier.edgeSwipeBackNavigation(
	enabled: Boolean,
	edgeWidth: Dp = 32.dp,
	triggerDistance: Dp = 96.dp,
	onBack: () -> Unit
): Modifier {
	if (!enabled) return this

		return pointerInput(enabled, edgeWidth, triggerDistance) {
		val edgeWidthPx = edgeWidth.toPx()
		val triggerDistancePx = triggerDistance.toPx()
		val touchSlop = viewConfiguration.touchSlop

		awaitEachGesture {
			val firstDown = awaitFirstDown(requireUnconsumed = false)

			if (firstDown.position.x > edgeWidthPx) {
				return@awaitEachGesture
			}

			var totalDx = 0f
			var totalDy = 0f
			var isCancelled = false
			var hasPassedSlop = false
			val pointerId = firstDown.id

			while (true) {
				val event = awaitPointerEvent(pass = PointerEventPass.Main)
				val change = event.changes.firstOrNull { it.id == pointerId } ?: continue

				if (!change.pressed) break

				val delta = change.position - change.previousPosition
				totalDx += delta.x
				totalDy += delta.y

				if (!hasPassedSlop && (abs(totalDx) + abs(totalDy)) < touchSlop) {
					continue
				}
				hasPassedSlop = true

				if (totalDx < 0f) {
					isCancelled = true
					break
				}

				val horizontalDominant = abs(totalDx) >= (abs(totalDy) * 0.75f)
				if (!horizontalDominant) {
					isCancelled = true
					break
				}

				if (totalDx > 0f) {
					change.consume()
				}
			}

			if (!isCancelled && totalDx >= triggerDistancePx) {
				onBack()
			}
		}
	}
}
