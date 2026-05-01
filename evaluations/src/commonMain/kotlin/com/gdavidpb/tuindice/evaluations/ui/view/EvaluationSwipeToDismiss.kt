package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags
import kotlin.math.roundToInt
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import tuindice.evaluations.generated.resources.Res
import tuindice.evaluations.generated.resources.label_evaluation_swipe_delete
import tuindice.evaluations.generated.resources.label_evaluation_swipe_edit

private val ActionsWidth = 224.dp

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

@Composable
private fun BoxScope.EvaluationActionsContainer(
	modifier: Modifier = Modifier,
	onEdit: () -> Unit,
	onDelete: () -> Unit
) {
	Box(
		modifier = modifier,
		contentAlignment = Alignment.CenterEnd
	) {
		EvaluationActions(
			modifier = Modifier
				.width(ActionsWidth)
				.fillMaxHeight(),
			onEdit = onEdit,
			onDelete = onDelete
		)
	}
}

@Composable
private fun EvaluationActions(
	modifier: Modifier = Modifier,
	onEdit: () -> Unit,
	onDelete: () -> Unit
) {
	Row(
		modifier = modifier,
		horizontalArrangement = Arrangement.End,
		verticalAlignment = Alignment.CenterVertically
	) {
		EvaluationActionButton(
			modifier = Modifier
				.weight(1f)
				.testTag(EvaluationsUiTags.EvaluationSwipeEditAction),
			text = stringResource(Res.string.label_evaluation_swipe_edit),
			containerColor = MaterialTheme.colorScheme.primaryContainer,
			contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
			shape = RoundedCornerShape(
				topStart = 8.dp,
				bottomStart = 8.dp,
				topEnd = 0.dp,
				bottomEnd = 0.dp
			),
			onClick = onEdit
		) {
			Icon(
				imageVector = Icons.Outlined.Edit,
				contentDescription = null
			)
		}

		EvaluationActionButton(
			modifier = Modifier
				.weight(1f)
				.testTag(EvaluationsUiTags.EvaluationSwipeDeleteAction),
			text = stringResource(Res.string.label_evaluation_swipe_delete),
			containerColor = MaterialTheme.colorScheme.errorContainer,
			contentColor = MaterialTheme.colorScheme.onErrorContainer,
			shape = RoundedCornerShape(
				topStart = 0.dp,
				bottomStart = 0.dp,
				topEnd = 8.dp,
				bottomEnd = 8.dp
			),
			onClick = onDelete
		) {
			Icon(
				imageVector = Icons.Default.Delete,
				contentDescription = null
			)
		}
	}
}

@Composable
private fun EvaluationActionButton(
	modifier: Modifier = Modifier,
	text: String,
	containerColor: Color,
	contentColor: Color,
	shape: Shape,
	onClick: () -> Unit,
	icon: @Composable () -> Unit
) {
	Box(
		modifier = modifier
			.background(
				color = containerColor,
				shape = shape
			)
			.clickable(onClick = onClick)
			.fillMaxHeight()
			.padding(horizontal = 10.dp),
		contentAlignment = Alignment.Center
	) {
		Row(
			horizontalArrangement = Arrangement.Center,
			verticalAlignment = Alignment.CenterVertically
		) {
			CompositionLocalProvider(LocalContentColor provides contentColor) {
				icon()
			}

			Text(
				modifier = Modifier.padding(start = 4.dp),
				text = text,
				color = contentColor,
				style = MaterialTheme.typography.labelMedium,
				fontWeight = FontWeight.SemiBold,
				maxLines = 1,
				overflow = TextOverflow.Clip
			)
		}
	}
}
