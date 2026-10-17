package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.model.ChatItem
import com.gdavidpb.tuindice.assistant.presentation.model.ChatPartItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing

// The agent's message: full width, with no bubble and no name. Its parts follow in order, then its actions.
@Composable
fun AssistantAgentMessageView(
	item: ChatItem.Agent,
	onAction: (AssistantChat.Action) -> Unit,
	modifier: Modifier = Modifier
) {
	Column(
		modifier = modifier.fillMaxWidth().testTag(AssistantUiTags.Message.agent(item.id)),
		verticalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Medium)
	) {
		item.parts.forEach { part ->
			when (part) {
				is ChatPartItem.Text -> Text(
					text = part.text,
					style = MaterialTheme.typography.bodyLarge,
					color = MaterialTheme.colorScheme.onSurface
				)

				is ChatPartItem.Component -> ChatComponentView(
					messageId = item.id,
					component = part.component,
					onAction = onAction
				)

				is ChatPartItem.IntentCard -> AssistantIntentCardView(card = part.card, onAction = onAction)
			}
		}
		AssistantMessageActionsView(
			messageId = item.id,
			useful = item.useful,
			onAction = onAction
		)
	}
}
