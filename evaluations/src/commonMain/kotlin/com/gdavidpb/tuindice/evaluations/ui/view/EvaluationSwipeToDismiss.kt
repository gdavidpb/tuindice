package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags
import com.gdavidpb.tuindice.evaluations.ui.model.EvaluationSwipeActions
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
	val coroutineScope = rememberCoroutineScope()
	val density = LocalDensity.current
	val actionsWidthPx = with(density) { revealWidth(hasAsk = actions.onAsk != null).toPx() }
	val offset = remember(actionsWidthPx, initiallyOpen) {
		Animatable(if (initiallyOpen) -actionsWidthPx else 0f)
	}

	suspend fun openActions() {
		offset.animateTo(
			targetValue = -actionsWidthPx,
			animationSpec = tween(durationMillis = 180)
		)
	}

	suspend fun closeActions() {
		offset.animateTo(
			targetValue = 0f,
			animationSpec = tween(durationMillis = 180)
		)
	}

	fun toggleActions() {
		coroutineScope.launch {
			if (offset.value < -1f) {
				closeActions()
			} else {
				openActions()
			}
		}
	}

	// Like Edit and Delete, the assistant's action closes the row once it has been taken.
	fun askAndClose(ask: () -> Unit) {
		ask()
		coroutineScope.launch { closeActions() }
	}

	Box(
		modifier = modifier
			.testTag(EvaluationsUiTags.EvaluationSwipeToDismissContainer)
			.fillMaxWidth()
			.pointerInput(actionsWidthPx) {
				detectHorizontalDragGestures(
					onHorizontalDrag = { _, dragAmount ->
						coroutineScope.launch {
							offset.snapTo((offset.value + dragAmount).coerceIn(-actionsWidthPx, 0f))
						}
					},
					onDragEnd = {
						coroutineScope.launch {
							if (offset.value <= -actionsWidthPx / 2f) {
								openActions()
							} else {
								closeActions()
							}
						}
					}
				)
			}
	) {
		if (offset.value < -1f) {
			EvaluationActionsContainer(
				modifier = Modifier
					.matchParentSize()
					.padding(horizontal = 16.dp, vertical = 8.dp),
				onAsk = actions.onAsk?.let { ask -> { askAndClose(ask) } },
				onEdit = {
					actions.onEdit()
					coroutineScope.launch { closeActions() }
				},
				onDelete = {
					actions.onDelete()
					coroutineScope.launch { closeActions() }
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
			content(::toggleActions)
		}
	}
}
