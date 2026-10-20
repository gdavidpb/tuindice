package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.base.ui.view.TuIndiceChevronLink
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_chat_component_detail

// The link that opens a component shown on its own sheet: «Ver detalle», with the arrow of the shared chevron link.
@Composable
fun ChatComponentDetailRowView(
	messageId: String,
	componentId: String,
	onClick: () -> Unit,
	modifier: Modifier = Modifier
) {
	TuIndiceChevronLink(
		text = stringResource(Res.string.assistant_chat_component_detail),
		onClick = onClick,
		modifier = modifier.testTag(AssistantUiTags.Component.detail(messageId, componentId))
	)
}
