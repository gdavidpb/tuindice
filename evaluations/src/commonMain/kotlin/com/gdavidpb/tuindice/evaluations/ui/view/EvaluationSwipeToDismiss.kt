package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.ReportComposition
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags
import com.gdavidpb.tuindice.evaluations.ui.model.EvaluationSwipeActions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

internal val ActionsWidth = 224.dp

// Each action keeps its width with two; the revealed area grows by one more with the assistant's action.
internal val ActionsWithAskWidth = 336.dp

internal fun actionsWidth(hasAsk: Boolean) = if (hasAsk) ActionsWithAskWidth else ActionsWidth

// The open card stops this far before the first action, so the card and its actions do not touch.
internal val ActionsGap = TuIndiceSpacing.Medium

// How far the card slides to open: the actions and the gap before them.
internal fun revealWidth(hasAsk: Boolean) = actionsWidth(hasAsk) + ActionsGap

@Composable
fun EvaluationSwipeToDismiss(
	modifier: Modifier = Modifier,
	initiallyOpen: Boolean = false,
	actions: EvaluationSwipeActions,
	content: @Composable (onActionsClick: () -> Unit) -> Unit
) {
	// Counts the recompositions of the row's swipe state, which a drag must not repeat on each of its frames.
	ReportComposition("evaluation-swipe")

	val coroutineScope = rememberCoroutineScope()
	val density = LocalDensity.current
	val actionsWidthPx = with(density) { revealWidth(hasAsk = actions.onAsk != null).toPx() }
	val offset = remember(actionsWidthPx, initiallyOpen) {
		SwipeOffset(if (initiallyOpen) -actionsWidthPx else 0f)
	}

	// Only crossing the threshold recomposes the row: the drag and the animation move the offset on every frame, and
	// nothing composed here reads it on each frame.
	val showsActions by remember(offset) {
		derivedStateOf { offset.value < -1f }
	}

	// Remembered, so the content receives the same callback while the row moves, and does not compose again for it.
	val toggleActions = remember(offset, actionsWidthPx, coroutineScope) {
		{
			offset.animateTo(
				scope = coroutineScope,
				target = if (offset.value < -1f) 0f else -actionsWidthPx
			)
		}
	}

	// Like Edit and Delete, the assistant's action closes the row once it has been taken.
	fun askAndClose(ask: () -> Unit) {
		ask()
		offset.animateTo(scope = coroutineScope, target = 0f)
	}

	Box(
		modifier = modifier
			.testTag(EvaluationsUiTags.EvaluationSwipeToDismissContainer)
			.fillMaxWidth()
			.dragToReveal(offset = offset, actionsWidthPx = actionsWidthPx, scope = coroutineScope)
	) {
		if (showsActions) {
			EvaluationActionsContainer(
				modifier = Modifier
					.matchParentSize()
					.padding(horizontal = 16.dp, vertical = 8.dp),
				onAsk = actions.onAsk?.let { ask -> { askAndClose(ask) } },
				onEdit = {
					actions.onEdit()
					offset.animateTo(scope = coroutineScope, target = 0f)
				},
				onDelete = {
					actions.onDelete()
					offset.animateTo(scope = coroutineScope, target = 0f)
				}
			)
		}

		Box(
			modifier = Modifier.offset {
				IntOffset(
					x = offset.value.roundToInt(),
					y = 0
				)
			}
		) {
			content(toggleActions)
		}
	}
}

// The drag moves the row's offset with each event, so no event launches a coroutine. A drag that starts while an
// animation runs takes over from it, and the drag ends by opening or closing the row. A cancelled drag settles the
// same way, so the row does not stay half open.
private fun Modifier.dragToReveal(
	offset: SwipeOffset,
	actionsWidthPx: Float,
	scope: CoroutineScope
): Modifier = pointerInput(actionsWidthPx, offset) {
	// Opens the row from half of the actions on and closes it before that, whether the drag ends or is cancelled.
	fun settle() {
		offset.animateTo(
			scope = scope,
			target = if (offset.value <= -actionsWidthPx / 2f) -actionsWidthPx else 0f
		)
	}

	detectHorizontalDragGestures(
		onDragStart = { offset.cancelAnimation() },
		onHorizontalDrag = { _, dragAmount ->
			offset.value = (offset.value + dragAmount).coerceIn(-actionsWidthPx, 0f)
		},
		onDragEnd = { settle() },
		onDragCancel = { settle() }
	)
}

// The offset the row is dragged and animated by. A drag writes it on each event, so no event launches a coroutine. An
// open or a close animates it in one coroutine, and the next gesture or animation cancels the one still running.
private class SwipeOffset(initial: Float) {
	private val state = mutableFloatStateOf(initial)
	private var animation: Job? = null

	var value: Float
		get() = state.floatValue
		set(value) {
			state.floatValue = value
		}

	fun cancelAnimation() {
		animation?.cancel()
	}

	fun animateTo(scope: CoroutineScope, target: Float) {
		cancelAnimation()

		val from = value

		animation = scope.launch {
			animate(
				initialValue = from,
				targetValue = target,
				animationSpec = tween(durationMillis = 180)
			) { current, _ ->
				state.floatValue = current
			}
		}
	}
}
