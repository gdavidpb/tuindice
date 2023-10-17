package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.DismissDirection
import androidx.compose.material3.DismissState
import androidx.compose.material3.DismissValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismiss
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import com.gdavidpb.tuindice.evaluations.R
import com.gdavidpb.tuindice.evaluations.utils.SCALE_EVALUATION_SWIPE_ICON_MAX
import com.gdavidpb.tuindice.evaluations.utils.SCALE_EVALUATION_SWIPE_ICON_MIN
import com.gdavidpb.tuindice.evaluations.utils.THRESHOLD_EVALUATION_SWIPE

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EvaluationSwipeToDismiss(
	isCompleted: Boolean,
	state: DismissState,
	dismissContent: @Composable RowScope.() -> Unit
) {
	SwipeToDismiss(
		state = state,
		background = {
			val direction = state.dismissDirection

			val color by animateColorAsState(
				targetValue = when (state.targetValue) {
					DismissValue.Default ->
						Color.Transparent

					DismissValue.DismissedToEnd ->
						MaterialTheme.colorScheme.primaryContainer

					DismissValue.DismissedToStart ->
						MaterialTheme.colorScheme.errorContainer
				},
				label = "SwipeToDismiss_animateColorAsState"
			)

			val icon = when (direction) {
				DismissDirection.StartToEnd ->
					if (isCompleted) Icons.AutoMirrored.Filled.Undo else Icons.Default.Done

				DismissDirection.EndToStart ->
					Icons.Default.Delete

				else -> null
			}

			val alignment = when (direction) {
				DismissDirection.StartToEnd ->
					Alignment.CenterStart

				DismissDirection.EndToStart ->
					Alignment.CenterEnd

				else -> null
			}

			val scale by animateFloatAsState(
				targetValue = when (state.targetValue) {
					DismissValue.Default ->
						0f

					DismissValue.DismissedToEnd, DismissValue.DismissedToStart ->
						if (state.progress < THRESHOLD_EVALUATION_SWIPE)
							SCALE_EVALUATION_SWIPE_ICON_MIN
						else
							SCALE_EVALUATION_SWIPE_ICON_MAX
				},
				label = "SwipeToDismiss_animateFloatAsState"
			)

			if (icon != null && alignment != null)
				Box(
					modifier = Modifier
						.fillMaxSize()
						.background(color),
					contentAlignment = alignment
				) {
					Icon(
						modifier = Modifier
							.padding(
								horizontal = dimensionResource(id = R.dimen.dp_24)
							)
							.scale(scale),
						imageVector = icon,
						contentDescription = ""
					)
				}
		},
		dismissContent = dismissContent
	)
}