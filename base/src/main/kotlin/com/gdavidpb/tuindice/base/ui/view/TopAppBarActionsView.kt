package com.gdavidpb.tuindice.base.ui.view

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.base.presentation.model.TopBarAction
import com.gdavidpb.tuindice.base.presentation.model.TopBarConfig

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

			enter togetherWith exit
		},
		label = "TopAppBarActionsViewAnimatedContent",
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