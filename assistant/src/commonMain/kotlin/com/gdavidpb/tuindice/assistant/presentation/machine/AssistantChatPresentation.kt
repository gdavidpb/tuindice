package com.gdavidpb.tuindice.assistant.presentation.machine

import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.mapper.AssistantChatTexts
import com.gdavidpb.tuindice.assistant.presentation.mapper.lastAgentSuggestions
import com.gdavidpb.tuindice.assistant.presentation.mapper.toChatItems
import com.gdavidpb.tuindice.assistant.presentation.model.ChatComposerItem
import com.gdavidpb.tuindice.assistant.presentation.model.ChatItem
import com.gdavidpb.tuindice.assistant.presentation.model.ChatNoticeItem
import com.gdavidpb.tuindice.assistant.presentation.model.ChatSuggestionItem

// The presentation of the chat, derived from the conversation and the extended state. Every screen state the rows
// produce goes through here, so the same inputs always show the same content.

internal suspend fun AssistantChatSession.presentedContent(
	texts: AssistantChatTexts,
	draftText: String,
	content: AssistantChat.State.Content
): AssistantChat.State.Content {
	val running = isRunning()
	val guided = guidedByMode || guidedByServer
	val composerEnabled = !guided && restriction == null

	return content.copy(
		items = presentedItems(texts),
		progressLabel = if (running) conversation?.activeTurn?.progressLabel else null,
		composer = ChatComposerItem(
			text = draftText,
			isEnabled = composerEnabled,
			isSendEnabled = composerEnabled && draftText.isNotBlank() && !running
		),
		suggestions = if (running) emptyList() else presentedSuggestions(),
		notice = presentedNotice(texts, guided),
		isGuided = guided
	)
}

// The notice that limits the chat now. A restriction outranks the quota, the quota outranks the guided mode, and the
// guided mode outranks a failed turn: each earlier one says more about why the chat is stopped.
internal suspend fun AssistantChatSession.presentedNotice(
	texts: AssistantChatTexts,
	guided: Boolean
): ChatNoticeItem? {
	val restricted = restriction
	val exhausted = quota
	val failure = turnFailure

	return when {
		restricted != null -> ChatNoticeItem.Restricted(
			message = texts.restricted(restricted.level),
			canAppeal = restricted.canAppeal
		)

		exhausted != null -> ChatNoticeItem.QuotaExhausted(
			message = texts.quotaExhausted(exhausted.retryAfterSeconds)
		)

		guided -> ChatNoticeItem.Guided(message = texts.guided())

		failure != null -> ChatNoticeItem.TurnFailed(
			message = failure.message,
			canRetry = failure.retryable && lastFailedSend != null
		)

		else -> null
	}
}

internal fun AssistantChatSession.presentedSuggestions(): List<ChatSuggestionItem> {
	return conversation?.lastAgentSuggestions()
		?.map { suggestion -> ChatSuggestionItem(id = suggestion.id, label = suggestion.label) }
		?: emptyList()
}

internal suspend fun AssistantChatSession.presentedItems(texts: AssistantChatTexts): List<ChatItem> {
	return conversation?.toChatItems(
		texts = texts,
		useful = usefulByMessage,
		failedMessages = failedMessages,
		selections = selectionByDraft,
		runningIntents = runningIntents
	) ?: emptyList()
}

// A turn that ended with a RunError: the student's last message is the one to retry, and it reads as failed.
internal suspend fun AssistantChatSession.runFailed(
	texts: AssistantChatTexts,
	event: AssistantChatInternalEvent.TurnRunFailed
) {
	val last = lastUserMessage()

	if (last != null) {
		failedMessages += last.id
		lastFailedSend = AssistantChatSession.FailedSend(text = last.text, suggestionId = null)
	}

	turnFailure = AssistantChatSession.TurnFailure(
		message = texts.turnFailure(event.code),
		retryable = event.retryable
	)
}
