package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.model.ChatItem
import com.gdavidpb.tuindice.assistant.presentation.model.ChatPartItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.style.chatEntrance
import com.gdavidpb.tuindice.assistant.ui.style.planAgentEntrance
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing

// The agent's message: full width, with no bubble and no name. Its parts follow in order, then its actions. When
// [animateEntrance] is set (the message arrived while the sheet was open) the text is typed in word by word and each
// card, then the actions, enter after it, one after another (AgentEntrancePlan).
@Composable
fun AssistantAgentMessageView(
	item: ChatItem.Agent,
	onAction: (AssistantChat.Action) -> Unit,
	modifier: Modifier = Modifier,
	animateEntrance: Boolean = false
) {
	val plan = remember(item.parts) { planAgentEntrance(item.parts) }

	Column(
		modifier = modifier.fillMaxWidth().testTag(AssistantUiTags.Message.agent(item.id)),
		verticalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Medium)
	) {
		item.parts.forEachIndexed { index, part ->
			when (part) {
				is ChatPartItem.Text -> AssistantRevealedTextView(
					text = part.text,
					animate = animateEntrance,
					delayMillis = plan.partDelays[index],
					style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface)
				)

				is ChatPartItem.Component -> ChatComponentView(
					messageId = item.id,
					item = part.item,
					onAction = onAction,
					modifier = Modifier.chatEntrance(animateEntrance, delayMillis = plan.partDelays[index])
				)

				is ChatPartItem.IntentCard -> AssistantIntentCardView(
					card = part.card,
					onAction = onAction,
					modifier = Modifier.chatEntrance(animateEntrance, delayMillis = plan.partDelays[index])
				)
			}
		}
		AssistantMessageActionsView(
			messageId = item.id,
			useful = item.useful,
			reported = item.reported,
			onAction = onAction,
			modifier = Modifier.chatEntrance(animateEntrance, delayMillis = plan.actionsDelay)
		)
	}
}
