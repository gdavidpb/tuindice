package com.gdavidpb.tuindice.ui.view

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.gdavidpb.tuindice.base.presentation.model.TopBarAction
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.base.ui.icon.rememberAssistantSparkleIcon

/**
 * The assistant's button of the top bar: the sparkle, drawn without a tint so it keeps its own amber. It is the first
 * place of the bar, and [contentDescription] is what a screen reader says for it.
 */
@Composable
fun TopBarAssistantButtonView(
	contentDescription: String,
	onClick: () -> Unit
) {
	IconButton(
		onClick = onClick,
		modifier = Modifier
			.testTag(BaseUiTags.topBarActionButton(TopBarAction.AssistantAction))
			.semantics {
				this.contentDescription = contentDescription
			}
	) {
		Icon(
			imageVector = rememberAssistantSparkleIcon(),
			contentDescription = null,
			tint = Color.Unspecified
		)
	}
}
