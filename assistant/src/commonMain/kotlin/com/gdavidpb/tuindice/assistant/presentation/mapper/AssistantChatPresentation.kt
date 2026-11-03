package com.gdavidpb.tuindice.assistant.presentation.mapper

import com.gdavidpb.tuindice.assistant.domain.model.AssistantAttachment
import com.gdavidpb.tuindice.assistant.domain.model.AssistantAttachmentFailure
import com.gdavidpb.tuindice.assistant.domain.model.AssistantAttachmentState
import com.gdavidpb.tuindice.assistant.domain.model.AssistantTurnPhase
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.machine.AssistantChatInternalEvent
import com.gdavidpb.tuindice.assistant.presentation.machine.AssistantChatSession
import com.gdavidpb.tuindice.assistant.presentation.machine.awaitsMemoryRead
import com.gdavidpb.tuindice.assistant.presentation.machine.failedSendOf
import com.gdavidpb.tuindice.assistant.presentation.machine.memoryAnswer
import com.gdavidpb.tuindice.assistant.presentation.machine.sendIsBusy
import com.gdavidpb.tuindice.assistant.presentation.model.ChatAttachmentItem
import com.gdavidpb.tuindice.assistant.presentation.model.ChatComposerInput
import com.gdavidpb.tuindice.assistant.presentation.model.ChatComposerItem
import com.gdavidpb.tuindice.assistant.presentation.model.ChatItem
import com.gdavidpb.tuindice.assistant.presentation.model.ChatNoticeItem
import com.gdavidpb.tuindice.assistant.presentation.model.ChatPartItem
import com.gdavidpb.tuindice.assistant.presentation.model.ChatSuggestionItem
import com.gdavidpb.tuindice.assistant.presentation.model.StillAppliesItem
import com.gdavidpb.tuindice.assistant.presentation.resource.AssistantChatTexts
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreenContext
import com.gdavidpb.tuindice.chatcontract.attachment.AttachmentLimits
import com.gdavidpb.tuindice.chatcontract.event.RunErrorCode
import kotlin.time.Clock

// The presentation of the chat, derived from the conversation and the extended state. Every screen state the rows
// produce goes through here, so the same inputs always show the same content.

internal suspend fun AssistantChatSession.presentedContent(
	texts: AssistantChatTexts,
	input: ChatComposerInput,
	content: AssistantChat.State.Content,
	clock: Clock
): AssistantChat.State.Content {
	val running = isRunning()
	// The criterion of the actions that send (PickSuggestion, RetryTurn): a send on its way or a running turn counts.
	val busy = sendIsBusy()
	val guided = guidedByMode || guidedByServer
	// A restriction whose end has passed no longer holds the composer: the next send reads the standing again.
	val restricted = restrictionAt(clock.now().toEpochMilliseconds())
	val items = presentedItems(texts, nowMillis = clock.now().toEpochMilliseconds())
	// Guided mode needs suggestions to keep asking. In open chat they belong only to the empty conversation.
	val showSuggestions = !busy && restricted == null && (guided || items.isEmpty())
	val composer = presentedComposer(
		draftText = input.text,
		draftRevision = input.revision,
		restricted = restricted,
		attachments = input.attachments,
		texts = texts
	)
	val trayFull = input.attachments.size >= AttachmentLimits.MAX_PER_MESSAGE
	// The label: the running turn's, else a wait (a send, or a turn that can be stopped).
	val turnLabel = if (running) conversation?.activeTurn?.progressLabel else null
	val waiting = if (sending || composer.isStoppable) texts.sending() else null

	return content.copy(
		items = items,
		progressLabel = turnLabel ?: waiting,
		progressAnchorId = progressAnchorOf(items = items, sending = sending),
		isSending = sending,
		// Once the student writes, the card steps aside for the rest of the chat: it is only for the empty chat.
		notificationOffer = notificationOfferShown && items.isEmpty() && !sending,
		composer = composer,
		suggestions = if (showSuggestions) presentedSuggestions(context = openedContext, guided = guided) else emptyList(),
		notice = presentedNotice(texts = texts, guided = guided, restricted = restricted, trayFull = trayFull, clock = clock),
		isGuided = guided
	)
}

// The item the progress goes under. A text turn follows the student's last question; a send whose answer has begun
// follows the agent's last item, since a send can start before its question is stored. Until then the progress stays at
// the end.
private fun progressAnchorOf(items: List<ChatItem>, sending: Boolean): String? {
	val last = items.lastOrNull()
	if (sending && last is ChatItem.Agent) return last.id
	return items.lastOrNull { it is ChatItem.User }?.id ?: last?.id
}

// The composer: the text the student writes, the attachments above it, and what the student can do with them. It takes
// the draft and the state of the chat only, so a change of the text presents this alone (ChangeText), not the
// conversation.
internal suspend fun AssistantChatSession.presentedComposer(
	draftText: String,
	draftRevision: Int,
	restricted: AssistantChatSession.Restriction?,
	attachments: List<AssistantAttachment>,
	texts: AssistantChatTexts
): ChatComposerItem {
	val guided = guidedByMode || guidedByServer
	val busy = isRunning() || sending
	val composerEnabled = !guided && restricted == null
	// A message sends once every attachment is kept by the service; an attachment still being read or sent holds it.
	val everyAttachmentKept = attachments.all { attachment -> attachment.state is AssistantAttachmentState.Ready }
	val hasRoom = attachments.size < AttachmentLimits.MAX_PER_MESSAGE

	return ChatComposerItem(
		text = draftText,
		textRevision = draftRevision,
		isEnabled = composerEnabled,
		isSendEnabled = composerEnabled && !busy && everyAttachmentKept &&
			(draftText.isNotBlank() || attachments.isNotEmpty()),
		showsAttach = attachmentsEnabled,
		isAttachEnabled = composerEnabled && !busy && hasRoom && attachmentsEnabled,
		attachments = attachments.presentedAttachments(texts = texts),
		isStoppable = stopTarget() != null
	)
}

// The tray of the message being written, as the student sees it: each photo with its state, and the reason it failed
// when it did. Only a failure that can clear on its own, such as a lost connection, offers the retry: the others would
// fail again, so the student takes the photo out instead.
internal suspend fun List<AssistantAttachment>.presentedAttachments(
	texts: AssistantChatTexts
): List<ChatAttachmentItem> {
	return map { attachment ->
		val failure = (attachment.state as? AssistantAttachmentState.Failed)?.reason

		ChatAttachmentItem(
			localId = attachment.localId,
			name = attachment.name,
			thumbnail = attachment.thumbnail,
			isUploading = attachment.state == AssistantAttachmentState.Preparing ||
				attachment.state == AssistantAttachmentState.Uploading,
			isFailed = failure != null,
			canRetry = failure == AssistantAttachmentFailure.Other,
			failureText = failure?.let { reason -> texts.attachmentFailure(reason) }
		)
	}
}

// The notice that limits the chat now: the chat's own notice, and once that holds none, the limit of the attachments.
// The limit shows once the tray is full or a pick was cut to its room, and no text turn has failed: a failure of a text
// turn keeps its notice and its retry.
internal suspend fun AssistantChatSession.presentedNotice(
	texts: AssistantChatTexts,
	guided: Boolean,
	restricted: AssistantChatSession.Restriction?,
	trayFull: Boolean,
	clock: Clock
): ChatNoticeItem? {
	return presentedChatNotice(texts = texts, guided = guided, restricted = restricted, clock = clock)
		?: if (attachmentLimitHit || trayFull) {
			ChatNoticeItem.TurnFailed(message = texts.attachmentLimit(), canRetry = false)
		} else {
			null
		}
}

// The notice of the chat's state. A restriction outranks the quota, the quota outranks the guided mode, and the guided
// mode outranks a failed turn: each earlier one says more about why the chat is stopped.
private suspend fun AssistantChatSession.presentedChatNotice(
	texts: AssistantChatTexts,
	guided: Boolean,
	restricted: AssistantChatSession.Restriction?,
	clock: Clock
): ChatNoticeItem? {
	val exhausted = quota
	val failure = turnFailure
	// A retry is offered only while no send is busy: RetryTurn is refused meanwhile, so a retry offered then does nothing.
	val busy = sendIsBusy()
	// The answer that failed keeps its notice, with the retry of that answer, for as long as the last failed send is its
	// own.
	val answer = recoveredFailure?.takeIf { recovered -> lastFailedSend?.clientTurnId == recovered.clientTurnId }

	return when {
		restricted != null -> ChatNoticeItem.Restricted(
			message = restrictionText(level = restricted.level, untilMillis = restricted.untilMillis, clock = clock),
			canAppeal = restricted.canAppeal
		)

		exhausted != null -> ChatNoticeItem.QuotaExhausted(
			message = texts.quotaExhausted(exhausted.retryAfterSeconds)
		)

		guided -> ChatNoticeItem.Guided(message = texts.guided())

		// While a send is on its way, the notice of the unconfirmed turn is not shown: the retry waits for the send.
		conversation?.activeTurn?.status == AssistantTurnPhase.UNCONFIRMED && !sending -> ChatNoticeItem.TurnFailed(
			message = texts.turnFailure(RunErrorCode.TRANSIENT, retryable = true),
			canRetry = lastFailedSend != null && !busy
		)

		failure != null -> ChatNoticeItem.TurnFailed(
			message = failure.message,
			canRetry = failure.retryable && lastFailedSend != null && !busy
		)

		answer != null -> ChatNoticeItem.TurnFailed(
			message = answer.failure.message,
			canRetry = answer.failure.retryable && !busy
		)

		else -> null
	}
}

// Guided follow-ups can only narrow the questions the opening screen can actually support.
internal fun AssistantChatSession.presentedSuggestions(
	context: AssistantScreenContext?,
	guided: Boolean
): List<ChatSuggestionItem> {
	val suitable = contextualSuggestions(context, guided)
	val agentIds = conversation?.lastAgentSuggestions().orEmpty().map { it.id }.toSet()
	val followUps = suitable.filter { it.id in agentIds }
	val replies = if (guided && followUps.isNotEmpty()) followUps else suitable

	return replies.map { reply -> ChatSuggestionItem(id = reply.id, label = reply.label) }
}

internal suspend fun AssistantChatSession.presentedItems(texts: AssistantChatTexts, nowMillis: Long): List<ChatItem> {
	val items = conversation?.toChatItems(texts = texts, session = this, nowMillis = nowMillis) ?: emptyList()

	return items.map { item ->
		if (item is ChatItem.Agent) {
			item.copy(
				parts = item.parts.map { part ->
					part.withMemoryAnswer(
						answer = this::memoryAnswer,
						pending = { id -> id in pendingMemories || awaitsMemoryRead(id) }
					)
				}
			)
		} else {
			item
		}
	}
}

// A «¿Sigue aplicando?» question shows the answer the session gives its memory (see AssistantChatSession.memoryAnswer).
// A question whose answer is being saved, or whose covering read has not come back, shows its buttons waiting.
internal fun ChatPartItem.withMemoryAnswer(answer: (String) -> Boolean?, pending: (String) -> Boolean): ChatPartItem {
	val question = (this as? ChatPartItem.Component)?.item as? StillAppliesItem ?: return this
	val memoryId = question.memoryId

	return ChatPartItem.Component(question.copy(answer = answer(memoryId), isPending = pending(memoryId)))
}

// A turn that ended with a RunError: the message it answers is the one to retry, and it reads as failed. Its retry
// is a new request (see retryFailedSend).
// A RunError of another turn than the one the chat sent is not the failure of that send: it shows nothing.
internal suspend fun AssistantChatSession.runFailed(
	texts: AssistantChatTexts,
	event: AssistantChatInternalEvent.TurnRunFailed
) {
	val sentTurnId = sendingTurnId

	if (sentTurnId != null && event.turnId != sentTurnId) return

	val failure = AssistantChatSession.TurnFailure(
		message = texts.turnFailure(event.code, event.retryable),
		retryable = event.retryable
	)
	val failed = failedSendOf(event.turnId)?.copy(runEnded = true)

	if (failed != null) {
		failedMessages += failed.clientTurnId
		lastFailedSend = failed
	}

	// The answer a new message waits for: the new message goes on, and the notice keeps the retry of this answer.
	if (failed != null && pendingSend?.turnId == event.turnId) {
		recoveredFailure = AssistantChatSession.RecoveredFailure(clientTurnId = failed.clientTurnId, failure = failure)
	} else {
		turnFailure = failure
	}
}
