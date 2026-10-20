package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.model.ChatComponentItem
import com.gdavidpb.tuindice.assistant.presentation.model.ChatRowsItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.model.CHAT_CARD_ROW_LIMIT
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing

// One component of an agent message. In a conversation, a component that opens on a sheet adds its link to the sheet:
// «Ver detalle», or, for a list past the row limit, «Ver las N …», since the conversation draws only the first
// rows. On its own sheet ([onSheet]), a component draws every row and adds no link.
@Composable
fun ChatComponentView(
	messageId: String,
	item: ChatComponentItem,
	onAction: (AssistantChat.Action) -> Unit,
	modifier: Modifier = Modifier,
	onSheet: Boolean = false
) {
	val rowLimit = if (onSheet) null else CHAT_CARD_ROW_LIMIT

	Column(
		modifier = modifier.fillMaxWidth().testTag(AssistantUiTags.Component.root(item.id)),
		verticalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Medium)
	) {
		ComponentRenderer(item = item, rowLimit = rowLimit, onAction = onAction)

		if (item is ChatRowsItem && rowLimit != null && item.rowCount > rowLimit) {
			ChatComponentSeeAllRowView(
				messageId = messageId,
				componentId = item.id,
				text = item.seeAllText.asString(),
				onClick = { onAction(AssistantChat.Action.OpenComponentSheet(messageId, item.id)) }
			)
		} else if (!onSheet && item.opensSheet) {
			ChatComponentDetailRowView(
				messageId = messageId,
				componentId = item.id,
				onClick = { onAction(AssistantChat.Action.OpenComponentSheet(messageId, item.id)) }
			)
		}
	}
}
