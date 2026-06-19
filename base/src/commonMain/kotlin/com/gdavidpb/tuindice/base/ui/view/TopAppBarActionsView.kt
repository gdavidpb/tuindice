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

@Composable
fun TopAppBarActionsView(
	topBarConfig: TopBarConfig?,
	onAction: (action: TopBarAction) -> Unit,
	actionContentDescription: @Composable (action: TopBarAction) -> String? = { null },
	actionIconContent: @Composable (action: TopBarAction) -> Unit
) {
	val currentOnAction = rememberUpdatedState(onAction)

	if (topBarConfig != null) {
		Row(modifier = Modifier.testTag(BaseUiTags.TopAppBarActionsContainer)) {
			topBarConfig.actions.forEach { action ->
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
		}
	}
}
