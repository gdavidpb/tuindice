package com.gdavidpb.tuindice.base.ui.view

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.base.presentation.model.TopBarAction
import com.gdavidpb.tuindice.base.presentation.model.TopBarConfig

@Composable
fun TopAppBarActionsView(
	topBarConfig: TopBarConfig,
	onAction: (action: TopBarAction) -> Unit
) {
	topBarConfig.actions.forEach { action ->
		IconButton(onClick = { onAction(action) }) {
			Icon(
				imageVector = action.icon,
				contentDescription = null
			)
		}
	}
}