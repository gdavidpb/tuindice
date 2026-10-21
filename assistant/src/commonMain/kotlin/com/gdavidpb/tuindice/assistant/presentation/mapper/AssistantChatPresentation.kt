package com.gdavidpb.tuindice.assistant.presentation.mapper

import com.gdavidpb.tuindice.assistant.domain.mapper.toScreenContext
import com.gdavidpb.tuindice.assistant.domain.model.AssistantConversationItem
import com.gdavidpb.tuindice.assistant.domain.model.AssistantTurnPhase
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.machine.AssistantChatInternalEvent
import com.gdavidpb.tuindice.assistant.presentation.machine.AssistantChatSession
import com.gdavidpb.tuindice.assistant.presentation.machine.lastUserMessage
import com.gdavidpb.tuindice.assistant.presentation.model.ChatComposerItem
import com.gdavidpb.tuindice.assistant.presentation.model.ChatItem
import com.gdavidpb.tuindice.assistant.presentation.model.ChatNoticeItem
import com.gdavidpb.tuindice.assistant.presentation.model.ChatPartItem
import com.gdavidpb.tuindice.assistant.presentation.model.ChatSuggestionItem
import com.gdavidpb.tuindice.assistant.presentation.model.StillAppliesItem
import com.gdavidpb.tuindice.assistant.presentation.resource.AssistantChatTexts
import com.gdavidpb.tuindice.chatcontract.context.ScreenId
import com.gdavidpb.tuindice.chatcontract.event.RunErrorCode
import com.gdavidpb.tuindice.chatcontract.message.SuggestedQuestions
import kotlin.time.Clock

// The presentation of the chat, derived from the conversation and the extended state. Every screen state the rows
// produce goes through here, so the same inputs always show the same content.

internal suspend fun AssistantChatSession.presentedContent(
	texts: AssistantChatTexts,
	draftText: String,
	draftRevision: Int,
	content: AssistantChat.State.Content,
	clock: Clock
): AssistantChat.State.Content {
	val running = isRunning()
	val extracting = photoExtracting
	val busy = running || sending
	val guided = guidedByMode || guidedByServer
	// A restriction whose end has passed no longer holds the composer: the next send reads the standing again.
	val restricted = restrictionAt(clock.now().toEpochMilliseconds())
	val composerEnabled = !guided && restricted == null
	// The screen is the one the chip shows: once the chip is removed, the questions are the general ones.
	val screen = content.context?.context?.toScreenContext()?.screen

	val items = presentedItems(texts, nowMillis = clock.now().toEpochMilliseconds())

	return content.copy(
		items = items,
		progressLabel = if (running) conversation?.activeTurn?.progressLabel else null,
		isExtracting = extracting,
		isSending = sending,
		// Once the student writes, the card steps aside for the rest of the sheet: it is only for the empty chat.
		notificationOffer = notificationOfferShown && items.isEmpty() && !sending,
		composer = ChatComposerItem(
			text = draftText,
			textRevision = draftRevision,
			isEnabled = composerEnabled,
			isSendEnabled = composerEnabled && draftText.isNotBlank() && !busy,
			isPhotoEnabled = composerEnabled && !busy && !extracting,
			isStoppable = stopTarget() != null
		),
		suggestions = if (busy) emptyList() else presentedSuggestions(screen = screen, guided = guided),
		notice = presentedNotice(texts = texts, guided = guided, restricted = restricted, clock = clock),
		isGuided = guided
	)
}

// The notice that limits the chat now. A restriction outranks the quota, the quota outranks the guided mode, and the
// guided mode outranks a failed turn: each earlier one says more about why the chat is stopped.
internal suspend fun AssistantChatSession.presentedNotice(
	texts: AssistantChatTexts,
	guided: Boolean,
	restricted: AssistantChatSession.Restriction?,
	clock: Clock
): ChatNoticeItem? {
	val exhausted = quota
	val failure = turnFailure

	return when {
		restricted != null -> ChatNoticeItem.Restricted(
			message = restrictionText(level = restricted.level, untilMillis = restricted.untilMillis, clock = clock),
			canAppeal = restricted.canAppeal
		)

		exhausted != null -> ChatNoticeItem.QuotaExhausted(
			message = texts.quotaExhausted(exhausted.retryAfterSeconds)
		)

		guided -> ChatNoticeItem.Guided(message = texts.guided())

		conversation?.activeTurn?.status == AssistantTurnPhase.UNCONFIRMED -> ChatNoticeItem.TurnFailed(
			message = texts.turnFailure(RunErrorCode.TRANSIENT, retryable = true),
			canRetry = lastFailedSend != null
		)

		failure != null -> ChatNoticeItem.TurnFailed(
			message = failure.message,
			canRetry = failure.retryable && lastFailedSend != null
		)

		else -> null
	}
}

// The replies the student can tap. The last agent message's come first. Without them, the questions of the screen the
// sheet came from are offered while the conversation has no agent message yet, and always in guided mode. Guided mode
// only takes questions of the shared catalog, so a reply of the agent outside it is not offered there.
internal fun AssistantChatSession.presentedSuggestions(
	screen: ScreenId?,
	guided: Boolean
): List<ChatSuggestionItem> {
	val agentReplies = conversation?.lastAgentSuggestions().orEmpty()
	val noAgentMessageYet = conversation?.items?.none { item -> item is AssistantConversationItem.Agent } ?: true

	val replies = when {
		guided ->
			agentReplies
				.filter { reply -> SuggestedQuestions.byId(reply.id) != null }
				.ifEmpty { SuggestedQuestions.forScreen(screen) }
		agentReplies.isNotEmpty() -> agentReplies
		noAgentMessageYet -> SuggestedQuestions.forScreen(screen)
		else -> emptyList()
	}

	return replies.map { reply -> ChatSuggestionItem(id = reply.id, label = reply.label) }
}

internal suspend fun AssistantChatSession.presentedItems(texts: AssistantChatTexts, nowMillis: Long): List<ChatItem> {
	val items = conversation?.toChatItems(texts = texts, session = this, nowMillis = nowMillis) ?: emptyList()

	return items.map { item ->
		if (item is ChatItem.Agent) item.copy(parts = item.parts.map { part -> part.withSavedAnswer(answeredMemories) }) else item
	}
}

// A «¿Sigue aplicando?» question the student already answered keeps the answer it was given.
internal fun ChatPartItem.withSavedAnswer(answers: Map<String, Boolean>): ChatPartItem {
	val question = (this as? ChatPartItem.Component)?.item as? StillAppliesItem ?: return this
	val answer = answers[question.memoryId] ?: return this

	return ChatPartItem.Component(question.copy(answer = answer))
}

// A turn that ended with a RunError: the student's last message is the one to retry, and it reads as failed.
internal suspend fun AssistantChatSession.runFailed(
	texts: AssistantChatTexts,
	event: AssistantChatInternalEvent.TurnRunFailed
) {
	val last = lastUserMessage()

	if (last != null) {
		failedMessages += last.id
		lastFailedSend = AssistantChatSession.FailedSend(
			text = last.text,
			suggestionId = followedSuggestionId,
			clientTurnId = last.id
		)
	}

	turnFailure = AssistantChatSession.TurnFailure(
		message = texts.turnFailure(event.code, event.retryable),
		retryable = event.retryable
	)
}
