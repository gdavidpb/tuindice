package com.gdavidpb.tuindice.assistant.presentation.transition

import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.machine.AssistantChatInternalEvent
import com.gdavidpb.tuindice.assistant.presentation.machine.AssistantChatMachine
import com.gdavidpb.tuindice.assistant.presentation.machine.AssistantChatSession
import com.gdavidpb.tuindice.assistant.presentation.machine.endSendUnconfirmed
import com.gdavidpb.tuindice.assistant.presentation.mapper.runFailed
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinitionBuilder
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost

// The outcomes of the jobs the content starts: a send, a confirmation, a dismissal, a restriction check, a feedback
// answer and an answer about a memory. Such a job outlives the content that started it, and the sheet can be on the
// consent when it ends. So each outcome is applied in both states: the session takes it, and [present] shows it. The
// content presents it; the consent keeps its state, and the content shows the session when the student comes back.

// The send's outcome: the acceptance of the turn is followed, a refusal is noticed, and an assistant that went off
// closes the sheet. A turn's failure and its end are kept for the content that shows them.
internal fun <S : AssistantChat.State> MachineDefinitionBuilder.StateTransitions<
	AssistantChat.State,
	S
>.sendOutcomeRows(
	machine: AssistantChatMachine,
	host: MachineHost<AssistantChat.Effect>,
	present: suspend (S) -> S
) {
	on<AssistantChatInternalEvent.TurnAccepted> { state, event ->
		machine.turnAccepted(host = host, event = event)
		present(state)
	}

	on<AssistantChatInternalEvent.SendRejected> { state, event ->
		machine.sendRejected(host = host, event = event)
		present(state)
	}

	onTo<AssistantChatInternalEvent.SendDisabled, AssistantChat.State.Unavailable>(
		emits = setOf(AssistantChat.Effect.Dismiss::class)
	) { _, _ ->
		host.sendEffect(AssistantChat.Effect.Dismiss)
		AssistantChat.State.Unavailable
	}

	on<AssistantChatInternalEvent.TurnRunFailed> { state, event ->
		machine.session.runFailed(texts = machine.texts, event = event)
		present(state)
	}

	on<AssistantChatInternalEvent.TurnStreamClosed> { state, event ->
		machine.session.endSendUnconfirmed(turnId = event.turnId)
		present(state)
	}

	// The restriction a standing read gives: a standing that no longer restricts releases the chat, and one that could
	// not be read keeps the restriction, with no level or end known.
	on<AssistantChatInternalEvent.RestrictionChecked> { state, event ->
		val standing = event.standing

		if (standing != null) machine.session.restriction = machine.access.restrictions.restrictionOf(standing)

		present(state)
	}
}

// The intent's outcome: a confirmation or a dismissal is over. The mark of a running confirmation goes when it ends,
// and the notice says the send failed when the service did not take it.
internal fun <S : AssistantChat.State> MachineDefinitionBuilder.StateTransitions<
	AssistantChat.State,
	S
>.intentOutcomeRows(
	machine: AssistantChatMachine,
	host: MachineHost<AssistantChat.Effect>,
	present: suspend (S) -> S
) {
	on<AssistantChatInternalEvent.IntentConfirmFinished> { state, event ->
		machine.session.runningIntents.remove(event.intentId)
		present(state)
	}

	// The notice that says the confirmation did not go through, without a retry: the card is back on its buttons.
	on<AssistantChatInternalEvent.IntentConfirmFailed> { state, event ->
		machine.session.runningIntents.remove(event.intentId)
		machine.session.turnFailure = AssistantChatSession.TurnFailure(
			message = machine.texts.confirmFailed(),
			retryable = false
		)
		present(state)
	}

	// The notice that says the dismissal did not reach the service, without a retry: the proposal stays open.
	on<AssistantChatInternalEvent.IntentDismissFailed> { state, _ ->
		machine.session.turnFailure = AssistantChatSession.TurnFailure(
			message = machine.texts.dismissFailed(),
			retryable = false
		)
		present(state)
	}

	on<AssistantChatInternalEvent.EnrollmentProofDelegated>(
		emits = setOf(AssistantChat.Effect.OpenEnrollmentProof::class)
	) { state, event ->
		machine.session.runningIntents.remove(event.intentId)
		host.sendEffect(AssistantChat.Effect.OpenEnrollmentProof)
		present(state)
	}
}

// The answers to the student's feedback and to the appeal: each one shows its snackbar.
internal fun <S : AssistantChat.State> MachineDefinitionBuilder.StateTransitions<
	AssistantChat.State,
	S
>.feedbackOutcomeRows(
	machine: AssistantChatMachine,
	host: MachineHost<AssistantChat.Effect>,
	present: suspend (S) -> S
) {
	on<AssistantChatInternalEvent.MessageRated>(
		emits = setOf(AssistantChat.Effect.ShowSnackBar::class)
	) { state, event ->
		host.sendEffect(AssistantChat.Effect.ShowSnackBar(message = event.message))
		state
	}

	on<AssistantChatInternalEvent.MessageRateFailed>(
		emits = setOf(AssistantChat.Effect.ShowSnackBar::class)
	) { state, event ->
		machine.session.rateRefused(messageId = event.messageId, rejected = event.rejected, previous = event.previous)
		host.sendEffect(AssistantChat.Effect.ShowSnackBar(message = event.message))
		present(state)
	}

	// The report is marked here, and only here: a saved report is the only one that changes the message.
	on<AssistantChatInternalEvent.MessageReported>(
		emits = setOf(AssistantChat.Effect.ShowSnackBar::class)
	) { state, event ->
		machine.session.reportedMessages += event.messageId
		host.sendEffect(AssistantChat.Effect.ShowSnackBar(message = event.message))
		present(state)
	}

	on<AssistantChatInternalEvent.MessageReportFailed>(
		emits = setOf(AssistantChat.Effect.ShowSnackBar::class)
	) { state, event ->
		host.sendEffect(AssistantChat.Effect.ShowSnackBar(message = event.message))
		state
	}

	on<AssistantChatInternalEvent.MessageReportRejected>(
		emits = setOf(AssistantChat.Effect.ShowSnackBar::class)
	) { state, event ->
		host.sendEffect(AssistantChat.Effect.ShowSnackBar(message = event.message))
		state
	}

	// Once the appeal is sent, the notice stops offering to send it again.
	on<AssistantChatInternalEvent.AppealSent>(
		emits = setOf(AssistantChat.Effect.ShowSnackBar::class)
	) { state, event ->
		host.sendEffect(AssistantChat.Effect.ShowSnackBar(message = event.message))
		machine.session.restriction = machine.session.restriction?.copy(canAppeal = false)
		present(state)
	}

	on<AssistantChatInternalEvent.AppealRejected>(
		emits = setOf(AssistantChat.Effect.ShowSnackBar::class)
	) { state, event ->
		host.sendEffect(AssistantChat.Effect.ShowSnackBar(message = event.message))
		state
	}
}
