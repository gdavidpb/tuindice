package com.gdavidpb.tuindice.base.ui.view

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.with
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.base.presentation.model.TopBarAction
import com.gdavidpb.tuindice.base.presentation.model.TopBarConfig

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun TopAppBarActionsView(
	topBarConfig: TopBarConfig?,
	onAction: (action: TopBarAction) -> Unit
) {
	AnimatedContent(
		targetState = topBarConfig,
		transitionSpec = {
			val enter = slideInHorizontally { x -> x }
			val exit = slideOutHorizontally { x -> -2 * x }

			enter with exit
		},
	) { targetState ->
		if (targetState != null)
			Row {
				targetState.actions.forEach { action ->
					IconButton(onClick = { onAction(action) }) {
						Icon(
							imageVector = action.icon,
							contentDescription = null
						)
					}
				}
			}
	}
}