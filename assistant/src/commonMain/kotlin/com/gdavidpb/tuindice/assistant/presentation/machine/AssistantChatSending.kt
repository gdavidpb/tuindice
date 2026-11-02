package com.gdavidpb.tuindice.assistant.presentation.machine

import com.gdavidpb.tuindice.assistant.domain.model.AssistantAttachment
import com.gdavidpb.tuindice.assistant.domain.model.AssistantTurnPhase
import com.gdavidpb.tuindice.assistant.domain.usecase.param.SendAssistantMessageParams
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreenContext
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost

// The sends of the chat: a new message the student writes or picks, and the retry of a failed send.
// Each one reaches the server through the machine's startTurn, the one path a turn takes. What differs is the
// request each one makes, and what waits for it.

// A new message. While the answer to the message before it is unconfirmed, that answer is recovered first: the
// message waits for the recovery, as a send does, and it is sent once the answer's final event comes. It is not
// sent when the recovery does not confirm the answer (see turnNotRecovered).
internal fun AssistantChatMachine.sendNewMessage(
	host: MachineHost<AssistantChat.Effect>,
	params: SendAssistantMessageParams,
	draftText: String?
) {
	val unconfirmed = session.conversation?.activeTurn?.takeIf { turn -> turn.status == AssistantTurnPhase.UNCONFIRMED }

	if (unconfirmed == null) {
		startTurn(host = host, params = params, draftText = draftText)
		return
	}

	beginSend(host)

	if (draftText != null) draft.replace("")
	takeAttachmentsFromTray(params.attachments)

	// A record keeps no bytes of a PDF: the send needs the ids its attachments were kept under, and nothing else.
	session.pendingSend = AssistantChatSession.PendingSend(
		turnId = unconfirmed.turnId,
		params = params.copy(attachments = params.attachments.withoutPdfContent()),
		restoreText = draftText,
		restoreAttachments = if (draftText != null) params.attachments.withoutPdfContent() else emptyList()
	)

	turns.recoverTurn(host = host, turnId = unconfirmed.turnId)
}

// The recovery confirmed the unconfirmed answer: the message that waited for it is sent now, with the text and the
// attachments it had, which the composer takes back if the send is refused.
internal fun AssistantChatMachine.turnRecovered(
	host: MachineHost<AssistantChat.Effect>,
	event: AssistantChatInternalEvent.TurnRecovered
) {
	val pending = session.pendingSend?.takeIf { waiting -> waiting.turnId == event.turnId } ?: return

	session.pendingSend = null

	startTurn(
		host = host,
		params = pending.params,
		draftText = null,
		restoreText = pending.restoreText,
		restoreAttachments = pending.restoreAttachments
	)
}

// The recovery did not confirm the unconfirmed answer: the message that waited for it is not sent.
// Its text and attachments go back to the composer, unless the student has written there meanwhile, as the text of
// a refused send does. The notice of the answer shows again, with its retry.
internal fun AssistantChatMachine.turnNotRecovered(event: AssistantChatInternalEvent.TurnNotRecovered) {
	val pending = session.pendingSend?.takeIf { waiting -> waiting.turnId == event.turnId } ?: return

	session.pendingSend = null
	session.sending = false

	if (draft.text.value.isEmpty()) pending.restoreText?.let(draft::replace)
	if (draft.attachments.value.isEmpty()) draft.setAttachments(pending.restoreAttachments)
}

// The retry of a failed send. A run that failed is retried as a new request: its answer is a new turn, with a
// client turn id of its own, and the failed question makes way for the question of that request. Any other failed
// send keeps the client turn id of its send, so the server answers with the same turn when it has it already. The
// attachments of the failed send go again: the service keeps them already, under their ids.
//
// A retry takes no text from the composer, so it gives no text back. It gives back only the attachments it carries
// that the tray holds, which a retry takes out of the tray as a send does: the message and the tray never show the
// same attachment at once. Usually the tray holds none of them, and a refused retry gives nothing back.
internal fun AssistantChatMachine.retryFailedSend(
	host: MachineHost<AssistantChat.Effect>,
	failed: AssistantChatSession.FailedSend,
	context: AssistantScreenContext?
) {
	val inTray = draft.attachments.value.map { attachment -> attachment.localId }.toSet()
	val restoreAttachments = failed.attachments.filter { attachment -> attachment.localId in inTray }

	if (!failed.runEnded) {
		startTurn(
			host = host,
			params = SendAssistantMessageParams(
				text = failed.text,
				suggestionId = failed.suggestionId,
				screenContext = context,
				clientTurnId = failed.clientTurnId,
				attachments = failed.attachments
			),
			draftText = null,
			restoreAttachments = restoreAttachments
		)

		return
	}

	session.lastFailedSend = null
	session.recoveredFailure = null
	session.failedMessages -= failed.clientTurnId

	// The question the retry replaces leaves the conversation in the same write that stores the retry's question, so the
	// two are not split by the end of the chat. The retry's session is the one it was decided in: a session that has
	// ended since leaves the conversation as it is.
	startTurn(
		host = host,
		params = SendAssistantMessageParams(
			text = failed.text,
			suggestionId = failed.suggestionId,
			screenContext = context,
			replacesMessageId = failed.clientTurnId,
			replacesSessionMark = session.conversation?.sessionMark,
			attachments = failed.attachments
		),
		draftText = null,
		restoreAttachments = restoreAttachments
	)
}

// The start of every send: the failures of the last one go, and the send is on its way. A restriction whose end
// has passed is read again from the server, which releases it or gives its new end; the send goes meanwhile, and
// the server refuses it if the restriction still holds.
internal fun AssistantChatMachine.beginSend(host: MachineHost<AssistantChat.Effect>) {
	session.turnFailure = null
	session.attachmentLimitHit = false
	session.sending = true
	session.sendingTurnId = null

	if (session.restriction != null && session.restrictionAt(access.restrictions.nowMillis()) == null) {
		session.restriction = null
		access.restrictions.restrictionStatus(host)
	}
}

// The attachments a send carries leave the tray: each one is either in the message or in the tray, never in both.
internal fun AssistantChatMachine.takeAttachmentsFromTray(attachments: List<AssistantAttachment>) {
	if (attachments.isEmpty()) return

	val carried = attachments.map { attachment -> attachment.localId }.toSet()

	draft.setAttachments(draft.attachments.value.filterNot { attachment -> attachment.localId in carried })
}
