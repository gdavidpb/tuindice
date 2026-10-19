package com.gdavidpb.tuindice.assistant.presentation.mapper

import com.gdavidpb.tuindice.assistant.domain.model.AssistantConversation
import com.gdavidpb.tuindice.assistant.domain.model.AssistantConversationItem
import com.gdavidpb.tuindice.assistant.presentation.model.ChatItem
import com.gdavidpb.tuindice.assistant.presentation.model.ChatPartItem
import com.gdavidpb.tuindice.assistant.presentation.model.IntentCardStatus
import com.gdavidpb.tuindice.chatcontract.message.AssistantMessage
import com.gdavidpb.tuindice.chatcontract.message.Component
import com.gdavidpb.tuindice.chatcontract.message.IntentPart
import com.gdavidpb.tuindice.chatcontract.message.SuggestedReply
import com.gdavidpb.tuindice.chatcontract.message.Suggestions
import com.gdavidpb.tuindice.chatcontract.message.Text

// The conversation as the chat shows it: the student's bubbles and the agent's messages. The suggestions of the last
// agent message are not a part of it; they are offered apart.
internal suspend fun AssistantConversation.toChatItems(
	texts: AssistantChatTexts,
	useful: Map<String, Boolean>,
	failedMessages: Set<String>,
	selections: Map<Pair<String, String>, Boolean>,
	runningIntents: Set<String>,
	nowMillis: Long
): List<ChatItem> {
	return items.map { item ->
		when (item) {
			is AssistantConversationItem.User -> ChatItem.User(
				id = item.message.id,
				text = item.message.text,
				isFailed = item.message.id in failedMessages
			)

			is AssistantConversationItem.Agent -> ChatItem.Agent(
				id = item.message.id,
				parts = item.message.toChatParts(texts, this, selections, runningIntents, nowMillis),
				useful = useful[item.message.id],
				copyText = item.message.copyText()
			)
		}
	}
}

// The suggestions of the last agent message: the replies the student can tap instead of typing.
internal fun AssistantConversation.lastAgentSuggestions(): List<SuggestedReply> {
	val lastAgent = items.filterIsInstance<AssistantConversationItem.Agent>().lastOrNull() ?: return emptyList()

	return lastAgent.message.parts
		.filterIsInstance<Suggestions>()
		.flatMap { suggestions -> suggestions.items }
}

private suspend fun AssistantMessage.toChatParts(
	texts: AssistantChatTexts,
	conversation: AssistantConversation,
	selections: Map<Pair<String, String>, Boolean>,
	runningIntents: Set<String>,
	nowMillis: Long
): List<ChatPartItem> {
	return parts.mapNotNull { part ->
		when (part) {
			is Text -> ChatPartItem.Text(text = part.text)

			is Component -> ChatPartItem.Component(item = part.component.toChatComponentItem(nowMillis))

			is IntentPart -> ChatPartItem.IntentCard(
				card = part.intent.toIntentCardItem(
					texts = texts,
					status = conversation.intentStatus(part.intent.id, runningIntents),
					selections = selections
				)
			)

			// The suggestions are offered apart, and a part this build does not know has nothing to show.
			else -> null
		}
	}
}

// The text the student copies: the texts of the message and the fallback of its components, in order.
private fun AssistantMessage.copyText(): String {
	return parts
		.mapNotNull { part ->
			when (part) {
				is Text -> part.text
				is Component -> part.component.fallbackText
				else -> null
			}
		}
		.filter { text -> text.isNotBlank() }
		.joinToString(separator = "\n")
}

// A resolved intent shows its outcome; one still running shows the progress; any other waits for the student.
private fun AssistantConversation.intentStatus(intentId: String, runningIntents: Set<String>): IntentCardStatus {
	val outcome = resolvedIntents[intentId]

	return when {
		outcome != null -> IntentCardStatus.Resolved(outcome)
		intentId in runningIntents -> IntentCardStatus.Running
		else -> IntentCardStatus.Open
	}
}
