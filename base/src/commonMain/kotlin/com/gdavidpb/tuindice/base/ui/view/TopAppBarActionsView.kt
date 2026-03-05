package com.gdavidpb.tuindice.base.ui.view

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.base.presentation.model.TopBarAction
import com.gdavidpb.tuindice.base.presentation.model.TopBarConfig
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

@Composable
fun TopAppBarActionsView(
	topBarConfig: TopBarConfig?,
	onAction: (action: TopBarAction) -> Unit,
	actionIconContent: @Composable (action: TopBarAction) -> Unit
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
		if (targetState != null) {
			Row(modifier = Modifier.testTag(BaseUiTags.TopAppBarActionsContainer)) {
				targetState.actions.forEach { action ->
					IconButton(
						modifier = Modifier.testTag(BaseUiTags.topBarActionButton(action)),
						onClick = { onAction(action) }
					) {
						actionIconContent(action)
					}
				}
			}
		}
	}
}
