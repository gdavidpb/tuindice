package com.gdavidpb.tuindice.base.ui.view

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.gdavidpb.tuindice.base.presentation.model.TopBarAction
import com.gdavidpb.tuindice.base.presentation.model.TopBarConfig
import com.gdavidpb.tuindice.base.ui.BaseUiTags

/**
 * The top bar of [topBarConfig], with all of its actions inline and no overflow menu. Nothing is drawn without a
 * config.
 */
@Composable
fun TopAppBarActionsView(
	topBarConfig: TopBarConfig?,
	onAction: (action: TopBarAction) -> Unit,
	actionContentDescription: @Composable (action: TopBarAction) -> String? = { null },
	actionIconContent: @Composable (action: TopBarAction) -> Unit
) {
	if (topBarConfig != null) {
		TopAppBarActionsView(
			actions = topBarConfig.actions,
			overflowActions = emptyList(),
			onAction = onAction,
			actionContentDescription = actionContentDescription,
			actionIconContent = actionIconContent
		)
	}
}

/**
 * Paints [actions] inline and, when [overflowActions] is not empty, the ⋮ menu with them after the inline ones.
 *
 * The menu's items take their text from [actionContentDescription], so every overflow action needs a description
 * (see [TopAppBarOverflowMenuView]).
 */
@Composable
fun TopAppBarActionsView(
	actions: List<TopBarAction>,
	overflowActions: List<TopBarAction>,
	onAction: (action: TopBarAction) -> Unit,
	actionContentDescription: @Composable (action: TopBarAction) -> String?,
	actionIconContent: @Composable (action: TopBarAction) -> Unit
) {
	val currentOnAction = rememberUpdatedState(onAction)

	Row(modifier = Modifier.testTag(BaseUiTags.TopAppBarActionsContainer)) {
		actions.forEach { action ->
			key(action) {
				val contentDescription = actionContentDescription(action)
				IconButton(
					onClick = {
						currentOnAction.value(action)
					},
					modifier = Modifier
						.testTag(BaseUiTags.topBarActionButton(action))
						.semantics {
							if (contentDescription != null) {
								this.contentDescription = contentDescription
							}
						}
				) {
					actionIconContent(action)
				}
			}
		}

		if (overflowActions.isNotEmpty()) {
			TopAppBarOverflowMenuView(
				actions = overflowActions,
				onAction = { action -> currentOnAction.value(action) },
				actionContentDescription = actionContentDescription
			)
		}
	}
}
