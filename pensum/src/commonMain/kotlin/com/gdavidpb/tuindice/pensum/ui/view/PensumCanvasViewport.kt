package com.gdavidpb.tuindice.pensum.ui.view

import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.geometry.Offset
import com.gdavidpb.tuindice.base.utils.extension.DecelerateEasing
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

// The canvas's viewport: its scale and its offsets. A gesture sets them at once, in the frame the touch moved, with no
// coroutine per touch event; an animation moves the three together, and any new move cancels the one running. They are
// read in the layout and draw phases (the content's graphicsLayer, the sticky header's placement, the minimap's
// drawing), so moving the viewport does not recompose the canvas.
internal class PensumCanvasViewport(
	initialScale: Float,
	initialOffsetX: Float,
	initialOffsetY: Float
) {
	val scale: MutableFloatState = mutableFloatStateOf(initialScale)
	val offsetX: MutableFloatState = mutableFloatStateOf(initialOffsetX)
	val offsetY: MutableFloatState = mutableFloatStateOf(initialOffsetY)

	private var animation: Job? = null

	val offset: Offset
		get() = Offset(offsetX.floatValue, offsetY.floatValue)

	fun snapTo(targetScale: Float, targetOffset: Offset) {
		stop()
		set(targetScale, targetOffset)
	}

	fun animateTo(
		scope: CoroutineScope,
		targetScale: Float,
		targetOffset: Offset,
		durationMillis: Int
	) {
		stop()
		val startScale = scale.floatValue
		val startOffset = offset
		animation = scope.launch {
			animate(
				initialValue = 0f,
				targetValue = 1f,
				animationSpec = tween(durationMillis = durationMillis, easing = DecelerateEasing)
			) { progress, _ ->
				set(
					scale = startScale + (targetScale - startScale) * progress,
					offset = startOffset + (targetOffset - startOffset) * progress
				)
			}
		}
	}

	fun stop() {
		animation?.cancel()
		animation = null
	}

	private fun set(scale: Float, offset: Offset) {
		this.scale.floatValue = scale
		offsetX.floatValue = offset.x
		offsetY.floatValue = offset.y
	}
}
