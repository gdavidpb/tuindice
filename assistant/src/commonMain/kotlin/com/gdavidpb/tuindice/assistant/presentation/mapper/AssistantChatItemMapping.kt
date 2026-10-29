package com.gdavidpb.tuindice.assistant.presentation.mapper

import com.gdavidpb.tuindice.assistant.domain.model.AssistantConversation
import com.gdavidpb.tuindice.assistant.domain.model.AssistantConversationItem
import com.gdavidpb.tuindice.assistant.presentation.machine.AssistantChatSession
import com.gdavidpb.tuindice.assistant.presentation.model.ChatItem
import com.gdavidpb.tuindice.assistant.presentation.model.ChatPartItem
import com.gdavidpb.tuindice.assistant.presentation.model.IntentCardStatus
import com.gdavidpb.tuindice.assistant.presentation.resource.AssistantChatTexts
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.chatcontract.component.UnknownChatComponent
import com.gdavidpb.tuindice.chatcontract.intent.AddEvaluationsArgs
import com.gdavidpb.tuindice.chatcontract.intent.Intent
import com.gdavidpb.tuindice.chatcontract.intent.OutcomeKind
import com.gdavidpb.tuindice.chatcontract.message.AssistantMessage
import com.gdavidpb.tuindice.chatcontract.message.Component
import com.gdavidpb.tuindice.chatcontract.message.IntentPart
import com.gdavidpb.tuindice.chatcontract.message.MessagePart
import com.gdavidpb.tuindice.chatcontract.message.SuggestedReply
import com.gdavidpb.tuindice.chatcontract.message.Suggestions
import com.gdavidpb.tuindice.chatcontract.message.Text
import com.gdavidpb.tuindice.chatcontract.message.UnknownMessagePart
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_intent_receipt_added_many
import tuindice.assistant.generated.resources.assistant_intent_receipt_added_one
import tuindice.assistant.generated.resources.assistant_intent_receipt_partial

// The conversation as the chat shows it: the student's bubbles and the agent's messages. The suggestions of the last
// agent message are not a part of it; they are offered apart.
internal suspend fun AssistantConversation.toChatItems(
	texts: AssistantChatTexts,
	session: AssistantChatSession,
	nowMillis: Long
): List<ChatItem> {
	return items.map { item ->
		when (item) {
			is AssistantConversationItem.User -> ChatItem.User(
				id = item.message.id,
				text = item.message.text,
				isFailed = item.message.id in session.failedMessages
			)

			is AssistantConversationItem.Agent -> ChatItem.Agent(
				id = item.message.id,
				parts = item.message.toChatParts(texts = texts, conversation = this, session = session, nowMillis = nowMillis),
				useful = session.usefulByMessage[item.message.id],
				reported = item.message.id in session.reportedMessages,
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
	session: AssistantChatSession,
	nowMillis: Long
): List<ChatPartItem> {
	val shown = parts.mapNotNull { part ->
		when (part) {
			is Text -> ChatPartItem.Text(text = part.text)

			// A list of evaluations draws the ones the device has with the cards of the Evaluations tab.
			is Component -> ChatPartItem.Component(
				item = part.component.toChatComponentItem(nowMillis = nowMillis, deviceCards = session.evaluationCards)
			)

			is IntentPart -> ChatPartItem.IntentCard(
				card = part.intent.toIntentCardItem(
					texts = texts,
					status = conversation.intentStatus(part.intent, session.runningIntents.keys),
					selections = session.selectionByDraft
				)
			)

			// The suggestions are offered apart, and a part this build does not know has nothing to show.
			else -> null
		}
	}

	// A part this build cannot show in full is left out or shown with its fallback text. One notice closes the
	// message, however many such parts it has.
	if (parts.none { part -> part.isNotShown() }) return shown

	return shown + ChatPartItem.Text(text = texts.partNotShown())
}

// A part this build cannot show in full: a part of a type it does not know, or a component of one.
private fun MessagePart.isNotShown(): Boolean {
	val component = (this as? Component)?.component

	return this is UnknownMessagePart || component is UnknownChatComponent
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

// A resolved intent shows its outcome and the rows it added. One resolved with an outcome this build does not know is
// closed, with no label. One still running shows the progress; any other waits for the student.
private fun AssistantConversation.intentStatus(intent: Intent, runningIntents: Set<String>): IntentCardStatus {
	val outcome = resolvedIntents[intent.id]

	return when {
		outcome != null -> IntentCardStatus.Resolved(
			outcome = outcome.toPresentation(detailCode = resolvedDetails[intent.id]),
			receipt = addedReceipt(intent, outcome)
		)
		intent.id in unknownOutcomeIntents -> IntentCardStatus.Closed
		intent.id in runningIntents -> IntentCardStatus.Running
		else -> IntentCardStatus.Open
	}
}

// The receipt of an addition of evaluations that landed: all the rows it marked, or some of them. Any other outcome or
// intent, and an addition whose count is not kept, has no receipt: the outcome's own label reads instead.
private fun AssistantConversation.addedReceipt(intent: Intent, outcome: OutcomeKind): UiText? {
	val count = resolvedCounts[intent.id]

	if (intent.args !is AddEvaluationsArgs || outcome != OutcomeKind.CONFIRMED || count == null) return null

	return when {
		count.applied == count.selected && count.applied > 0 -> chatCardCountText(
			count = count.applied,
			one = Res.string.assistant_intent_receipt_added_one,
			many = Res.string.assistant_intent_receipt_added_many
		)

		count.applied in 1 until count.selected -> UiText.Resource(
			resource = Res.string.assistant_intent_receipt_partial,
			args = listOf(count.applied, count.selected)
		)

		else -> null
	}
}
