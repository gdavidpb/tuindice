package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags
import com.gdavidpb.tuindice.evaluations.utils.THRESHOLD_EVALUATION_SWIPE
import kotlin.math.abs
import org.jetbrains.compose.resources.stringResource
import tuindice.evaluations.generated.resources.Res
import tuindice.evaluations.generated.resources.label_evaluation_swipe_delete
import tuindice.evaluations.generated.resources.label_evaluation_swipe_edit

@Composable
fun EvaluationSwipeToDismiss(
	state: SwipeToDismissBoxState,
	onDismiss: (SwipeToDismissBoxValue) -> Unit,
	dismissContent: @Composable RowScope.() -> Unit
) {
	val labelWidth = remember {
		mutableFloatStateOf(0f)
	}
	val dismissWidth = remember {
		mutableFloatStateOf(0f)
	}

	val backgroundInfo = getBackgroundInfo(
		state = state,
		labelWidth = labelWidth.floatValue,
		dismissWidth = dismissWidth.floatValue
	)

	SwipeToDismissBox(
		modifier = Modifier.testTag(EvaluationsUiTags.EvaluationSwipeToDismissContainer),
		state = state,
		onDismiss = onDismiss,
		backgroundContent = {
			val (color, icon, text, alignment, offset) = backgroundInfo

			Box(
				modifier = Modifier
					.fillMaxSize()
					.background(color)
					.onPlaced { layout ->
						dismissWidth.floatValue = layout.size.width.toFloat()
					},
				contentAlignment = alignment ?: Alignment.Center
			) {
				if (icon != null &&
					text != null &&
					offset != null
				) {
					Row(
						modifier = Modifier
							.offset(x = offset)
							.onPlaced { layout ->
								labelWidth.floatValue = layout.size.width.toFloat()
							}
					) {
						Icon(
							modifier = Modifier
								.padding(end = 4.dp),
							imageVector = icon,
							contentDescription = null
						)

						Text(text = text)
					}
				}
			}
		},
		content = dismissContent
	)
}

@Composable
private fun getBackgroundInfo(
	state: SwipeToDismissBoxState,
	labelWidth: Float,
	dismissWidth: Float
): BackgroundInfo {
	val revealRange = THRESHOLD_EVALUATION_SWIPE
		.coerceAtLeast(.01f)
	val swipeProgress = if (dismissWidth > 0f)
		runCatching { abs(state.requireOffset()) / dismissWidth }
			.getOrDefault(0f)
	else
		0f

	val targetAlpha = when (state.dismissDirection) {
		SwipeToDismissBoxValue.Settled -> 0f
		else -> (swipeProgress / revealRange)
			.coerceIn(0f, 1f)
	}

	val color by animateColorAsState(
		targetValue = when (state.dismissDirection) {
			SwipeToDismissBoxValue.StartToEnd ->
				MaterialTheme.colorScheme.primaryContainer.copy(alpha = targetAlpha)

			SwipeToDismissBoxValue.EndToStart ->
				MaterialTheme.colorScheme.errorContainer.copy(alpha = targetAlpha)

			else ->
				MaterialTheme.colorScheme.background
		},
		animationSpec = tween(durationMillis = 200),
		label = "SwipeToDismiss_animateColorAsState"
	)

	return when (state.dismissDirection) {
		SwipeToDismissBoxValue.StartToEnd -> BackgroundInfo(
			color = color,
			icon = Icons.Outlined.Edit,
			text = stringResource(Res.string.label_evaluation_swipe_edit),
			alignment = Alignment.CenterStart,
			offset = getOffset(
				progress = state.progress,
				direction = -1f,
				width = labelWidth,
				padding = 8.dp
			)
		)

		SwipeToDismissBoxValue.EndToStart -> BackgroundInfo(
			color = color,
			icon = Icons.Default.Delete,
			text = stringResource(Res.string.label_evaluation_swipe_delete),
			alignment = Alignment.CenterEnd,
			offset = getOffset(
				progress = state.progress,
				direction = 1f,
				width = labelWidth,
				padding = 8.dp
			)
		)

		else -> BackgroundInfo(
			color = color,
			icon = null,
			text = null,
			alignment = null,
			offset = 0.dp
		)
	}
}

private fun getOffset(
	progress: Float,
	direction: Float,
	width: Float,
	padding: Dp
): Dp {
	return (width * direction * (THRESHOLD_EVALUATION_SWIPE - progress))
		.let { offset ->
			if (direction < 0)
				offset.coerceAtMost(padding.value)
			else
				offset.coerceAtLeast(-padding.value)
		}
		.dp
}

private data class BackgroundInfo(
	val color: Color,
	val icon: ImageVector?,
	val text: String?,
	val alignment: Alignment?,
	val offset: Dp?
)
