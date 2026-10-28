package com.gdavidpb.tuindice.assistant.presentation.machine

import com.gdavidpb.tuindice.assistant.domain.model.AssistantTurnPhase
import com.gdavidpb.tuindice.assistant.domain.usecase.param.SendAssistantMessageParams
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreenContext
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost

// The sends of the chat sheet: a new message the student writes or picks, and the retry of a failed send.
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

	session.pendingSend = AssistantChatSession.PendingSend(
		turnId = unconfirmed.turnId,
		params = params,
		restoreText = draftText
	)

	turns.recoverTurn(host = host, turnId = unconfirmed.turnId)
}

// The recovery confirmed the unconfirmed answer: the message that waited for it is sent now, with the text it had.
internal fun AssistantChatMachine.turnRecovered(
	host: MachineHost<AssistantChat.Effect>,
	event: AssistantChatInternalEvent.TurnRecovered
) {
	val pending = session.pendingSend?.takeIf { waiting -> waiting.turnId == event.turnId } ?: return

	session.pendingSend = null

	startTurn(host = host, params = pending.params, draftText = null, restoreText = pending.restoreText)
}

// The recovery did not confirm the unconfirmed answer: the message that waited for it is not sent.
// Its text goes back to the composer, unless the student has written there meanwhile, as the text of
// a refused send does. The notice of the answer shows again, with its retry.
internal fun AssistantChatMachine.turnNotRecovered(event: AssistantChatInternalEvent.TurnNotRecovered) {
	val pending = session.pendingSend?.takeIf { waiting -> waiting.turnId == event.turnId } ?: return

	session.pendingSend = null
	session.sending = false

	if (draft.text.value.isEmpty()) pending.restoreText?.let(draft::replace)
}

// The retry of a failed send. A run that failed is retried as a new request: its answer is a new turn, with a
// client turn id of its own, and the failed question makes way for the question of that request. Any other failed
// send keeps the client turn id of its send, so the server answers with the same turn when it has it already.
internal fun AssistantChatMachine.retryFailedSend(
	host: MachineHost<AssistantChat.Effect>,
	failed: AssistantChatSession.FailedSend,
	context: AssistantScreenContext?
) {
	if (!failed.runEnded) {
		startTurn(
			host = host,
			params = SendAssistantMessageParams(
				text = failed.text,
				suggestionId = failed.suggestionId,
				screenContext = context,
				clientTurnId = failed.clientTurnId
			),
			draftText = null
		)

		return
	}

	session.lastFailedSend = null
	session.recoveredFailure = null
	session.failedMessages -= failed.clientTurnId

	// The question the retry replaces leaves the conversation in the same write that stores the retry's question, so the
	// two are not split by the end of the sheet. The retry's session is the one it was decided in: a session that has
	// ended since leaves the conversation as it is.
	startTurn(
		host = host,
		params = SendAssistantMessageParams(
			text = failed.text,
			suggestionId = failed.suggestionId,
			screenContext = context,
			replacesMessageId = failed.clientTurnId,
			replacesSessionMark = session.conversation?.sessionMark
		),
		draftText = null
	)
}

// The start of every send: the failures of the last one go, and the send is on its way. A restriction whose end
// has passed is read again from the server, which releases it or gives its new end; the send goes meanwhile, and
// the server refuses it if the restriction still holds.
internal fun AssistantChatMachine.beginSend(host: MachineHost<AssistantChat.Effect>) {
	session.turnFailure = null
	session.photoFailure = null
	session.sending = true
	session.sendingTurnId = null

	if (session.restriction != null && session.restrictionAt(access.restrictions.nowMillis()) == null) {
		session.restriction = null
		access.restrictions.restrictionStatus(host)
	}
}
