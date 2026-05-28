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
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

internal val ActionsWidth = 224.dp

@Composable
fun EvaluationSwipeToDismiss(
	modifier: Modifier = Modifier,
	initiallyOpen: Boolean = false,
	onEdit: () -> Unit,
	onDelete: () -> Unit,
	content: @Composable (onActionsClick: () -> Unit) -> Unit
) {
	val coroutineScope = rememberCoroutineScope()
	val density = LocalDensity.current
	val actionsWidthPx = with(density) { ActionsWidth.toPx() }
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
				onEdit = {
					onEdit()
					coroutineScope.launch { closeActions() }
				},
				onDelete = {
					onDelete()
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
