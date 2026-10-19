package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.model.ChatComponentItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing

// One component of an agent message. A component that opens on a sheet adds its «Ver detalle» row, except on its own
// sheet, where [showsDetailRow] is false.
@Composable
fun ChatComponentView(
	messageId: String,
	item: ChatComponentItem,
	onAction: (AssistantChat.Action) -> Unit,
	modifier: Modifier = Modifier,
	showsDetailRow: Boolean = true
) {
	Column(
		modifier = modifier.fillMaxWidth().testTag(AssistantUiTags.Component.root(item.id)),
		verticalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Medium)
	) {
		ComponentRenderer(item = item, onAction = onAction)

		if (showsDetailRow && item.opensSheet) {
			ChatComponentDetailRowView(
				messageId = messageId,
				componentId = item.id,
				onClick = { onAction(AssistantChat.Action.OpenComponentSheet(messageId, item.id)) }
			)
		}
	}
}
