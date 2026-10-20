package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.base.ui.view.TuIndiceChevronLink

// The link that opens a list on its sheet for the rows the conversation does not draw: «Ver las 12 materias». It
// takes the place of «Ver detalle» under a list past its row limit.
@Composable
fun ChatComponentSeeAllRowView(
	messageId: String,
	componentId: String,
	text: String,
	onClick: () -> Unit,
	modifier: Modifier = Modifier
) {
	TuIndiceChevronLink(
		text = text,
		onClick = onClick,
		modifier = modifier.testTag(AssistantUiTags.Component.seeAll(messageId, componentId))
	)
}
