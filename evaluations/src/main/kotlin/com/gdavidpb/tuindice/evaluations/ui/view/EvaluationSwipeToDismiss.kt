package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.evaluations.R
import com.gdavidpb.tuindice.evaluations.utils.THRESHOLD_EVALUATION_SWIPE

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EvaluationSwipeToDismiss(
	state: SwipeToDismissBoxState,
	dismissContent: @Composable RowScope.() -> Unit
) {
	val boxWidth = remember {
		mutableFloatStateOf(0f)
	}

	val backgroundInfo = getBackgroundInfo(
		state = state,
		width = boxWidth.floatValue
	)

	SwipeToDismissBox(
		state = state,
		backgroundContent = {
			val (color, icon, text, alignment, offset) = backgroundInfo

			if (icon != null &&
				text != null &&
				alignment != null &&
				offset != null
			) {
				Box(
					modifier = Modifier
						.fillMaxSize()
						.background(color),
					contentAlignment = alignment
				) {
					Row(
						modifier = Modifier
							.offset(x = offset)
							.onPlaced { layout ->
								boxWidth.floatValue = layout.size.width.toFloat()
							}
					) {
						Icon(
							modifier = Modifier
								.padding(
									end = dimensionResource(id = R.dimen.dp_4)
								),
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
@OptIn(ExperimentalMaterial3Api::class)
private fun getBackgroundInfo(state: SwipeToDismissBoxState, width: Float): BackgroundInfo {
	val color by animateColorAsState(
		targetValue = when (state.targetValue) {
			SwipeToDismissBoxValue.StartToEnd ->
				MaterialTheme.colorScheme.primaryContainer

			SwipeToDismissBoxValue.EndToStart ->
				MaterialTheme.colorScheme.errorContainer

			else ->
				Color.Transparent
		},
		label = "SwipeToDismiss_animateColorAsState"
	)

	return when (state.dismissDirection) {
		SwipeToDismissBoxValue.StartToEnd -> BackgroundInfo(
			color = color,
			icon = Icons.Outlined.Edit,
			text = stringResource(id = R.string.label_evaluation_swipe_edit),
			alignment = Alignment.CenterStart,
			offset = getOffset(
				progress = state.progress,
				direction = -1f,
				width = width
			)
		)

		SwipeToDismissBoxValue.EndToStart -> BackgroundInfo(
			color = color,
			icon = Icons.Default.Delete,
			text = stringResource(id = R.string.label_evaluation_swipe_delete),
			alignment = Alignment.CenterEnd,
			offset = getOffset(
				progress = state.progress,
				direction = 1f,
				width = width
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

@Composable
@ReadOnlyComposable
private fun getOffset(progress: Float, direction: Float, width: Float): Dp {
	val padding = LocalContext.current.resources.getDimension(R.dimen.dp_8)

	return (width * direction * (THRESHOLD_EVALUATION_SWIPE - progress))
		.let { offset ->
			if (direction < 0)
				offset.coerceAtMost(padding)
			else
				offset.coerceAtLeast(-padding)
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