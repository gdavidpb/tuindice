package com.gdavidpb.tuindice.assistant.presentation.transition

import com.gdavidpb.tuindice.assistant.domain.usecase.error.ExtractEvaluationsFromPhotoError
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.machine.AssistantChatInternalEvent
import com.gdavidpb.tuindice.assistant.presentation.machine.AssistantChatMachine
import com.gdavidpb.tuindice.assistant.presentation.machine.AssistantChatSession
import com.gdavidpb.tuindice.assistant.presentation.machine.runFailed
import com.gdavidpb.tuindice.assistant.presentation.model.ChatItem
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinitionBuilder
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost

private typealias ContentTransitions = MachineDefinitionBuilder.StateTransitions<
	AssistantChat.State,
	AssistantChat.State.Content
>

// The sheet with a conversation. The student's actions are the rows of the first functions; the results of the async
// commands are the rows of the last ones. Every row keeps its target: content stays content, or the sheet closes.
internal fun MachineDefinitionBuilder<AssistantChat.State>.assistantChatContentTransitions(
	machine: AssistantChatMachine,
	host: MachineHost<AssistantChat.Effect>
) {
	from<AssistantChat.State.Content> {
		// Leaving the content stops observing the conversation and following its turn.
		onExit { machine.leaveContent() }

		contentComposerRows(machine, host)
		contentPhotoRows(machine, host)
		contentMessageRows(machine, host)
		contentIntentRows(machine, host)
		contentObservationRows(machine, host)
		contentOutcomeRows(machine, host)
	}
}

private fun ContentTransitions.contentComposerRows(
	machine: AssistantChatMachine,
	host: MachineHost<AssistantChat.Effect>
) {
	on<AssistantChat.Action.ChangeText> { state, action ->
		if (!state.composer.isEnabled) return@on state

		machine.draft.setText(action.text)
		machine.refreshed(state)
	}

	on<AssistantChat.Action.Send> { state, _ ->
		if (!state.composer.isSendEnabled) return@on state

		val draftText = machine.draft.text.value

		machine.startTurn(
			host = host,
			text = draftText.trim(),
			suggestionId = null,
			context = state.context?.context,
			draftText = draftText
		)
		machine.refreshed(state)
	}

	on<AssistantChat.Action.PickSuggestion> { state, action ->
		val text = machine.session.suggestionText(action.suggestionId)

		if (text == null || machine.session.isRunning()) return@on state

		machine.startTurn(
			host = host,
			text = text,
			suggestionId = action.suggestionId,
			context = state.context?.context,
			draftText = machine.draft.text.value
		)
		machine.refreshed(state)
	}

	on<AssistantChat.Action.RemoveContext> { state, _ -> machine.refreshed(state.copy(context = null)) }

	on<AssistantChat.Action.RetryTurn> { state, _ ->
		val failed = machine.session.lastFailedSend

		if (failed == null || machine.session.isRunning()) return@on state

		machine.startTurn(
			host = host,
			text = failed.text,
			suggestionId = failed.suggestionId,
			context = state.context?.context,
			draftText = null
		)
		machine.refreshed(state)
	}
}

private fun ContentTransitions.contentPhotoRows(
	machine: AssistantChatMachine,
	host: MachineHost<AssistantChat.Effect>
) {
	on<AssistantChat.Action.RequestPhoto>(
		emits = setOf(AssistantChat.Effect.OpenCamera::class, AssistantChat.Effect.OpenPhotoPicker::class)
	) { state, action ->
		if (state.composer.isPhotoEnabled) {
			host.sendEffect(
				if (action.fromCamera) AssistantChat.Effect.OpenCamera else AssistantChat.Effect.OpenPhotoPicker
			)
		}

		state
	}

	on<AssistantChat.Action.AttachPhoto> { state, action ->
		if (!state.composer.isPhotoEnabled) return@on state

		machine.session.turnFailure = null
		machine.session.photoExtracting = true
		machine.turns.extractPhoto(host = host, file = action.file, context = state.context?.context)
		machine.refreshed(state)
	}

	on<AssistantChatInternalEvent.PhotoExtracted> { state, _ ->
		machine.session.photoExtracting = false
		machine.refreshed(state)
	}

	on<AssistantChatInternalEvent.PhotoRejected> { state, event ->
		machine.photoRejected(host = host, event = event)
		machine.refreshed(state)
	}
}

// A photo that was not read. Its notices are the turn's: the quota, the guided mode, a restriction, or a failure the
// student can see. A photo has no text to send again, so its failure is never retryable.
private suspend fun AssistantChatMachine.photoRejected(
	host: MachineHost<AssistantChat.Effect>,
	event: AssistantChatInternalEvent.PhotoRejected
) {
	session.photoExtracting = false

	when (val error = event.error) {
		is ExtractEvaluationsFromPhotoError.QuotaExhausted -> {
			session.quota = AssistantChatSession.Quota(retryAfterSeconds = error.retryAfterSeconds)
		}

		ExtractEvaluationsFromPhotoError.Restricted -> {
			session.restriction = AssistantChatSession.Restriction(level = null, canAppeal = false)
			access.restrictionStatus(host)
		}

		ExtractEvaluationsFromPhotoError.GuidedOnly -> session.guidedByServer = true

		else -> session.turnFailure = AssistantChatSession.TurnFailure(
			message = texts.sendFailed(),
			retryable = false
		)
	}
}

private fun ContentTransitions.contentMessageRows(
	machine: AssistantChatMachine,
	host: MachineHost<AssistantChat.Effect>
) {
	on<AssistantChat.Action.CopyMessage>(emits = setOf(AssistantChat.Effect.CopyText::class)) { state, action ->
		val agent = state.items
			.filterIsInstance<ChatItem.Agent>()
			.firstOrNull { item -> item.id == action.messageId }

		if (agent != null) host.sendEffect(AssistantChat.Effect.CopyText(text = agent.copyText))

		state
	}

	on<AssistantChat.Action.RateMessage> { state, action ->
		machine.session.usefulByMessage[action.messageId] = action.useful
		machine.feedback.rate(host = host, messageId = action.messageId, useful = action.useful)
		machine.refreshed(state)
	}

	on<AssistantChat.Action.ReportMessage> { state, action ->
		machine.feedback.report(
			host = host,
			messageId = action.messageId,
			reason = action.reason,
			comment = action.comment
		)
		state
	}

	on<AssistantChat.Action.AnswerStillApplies> { state, action ->
		machine.feedback.answerStillApplies(host = host, memoryId = action.memoryId, applies = action.applies)
		state
	}

	on<AssistantChat.Action.Appeal> { state, action ->
		machine.access.appeal(host = host, text = action.text)
		state
	}

	on<AssistantChat.Action.OpenNavTarget>(
		emits = setOf(AssistantChat.Effect.NavigateTo::class, AssistantChat.Effect.Dismiss::class)
	) { state, action ->
		host.sendEffect(AssistantChat.Effect.NavigateTo(target = action.target, entityRefs = action.entityRefs))
		host.sendEffect(AssistantChat.Effect.Dismiss)
		state
	}

	on<AssistantChat.Action.OpenComponentSheet>(
		emits = setOf(AssistantChat.Effect.OpenComponentSheet::class)
	) { state, action ->
		host.sendEffect(
			AssistantChat.Effect.OpenComponentSheet(messageId = action.messageId, componentId = action.componentId)
		)
		state
	}
}

private fun ContentTransitions.contentIntentRows(
	machine: AssistantChatMachine,
	host: MachineHost<AssistantChat.Effect>
) {
	on<AssistantChat.Action.ConfirmIntent> { state, action ->
		machine.confirmIntent(host = host, intentId = action.intentId)
		machine.refreshed(state)
	}

	on<AssistantChat.Action.DismissIntent> { state, action ->
		machine.intents.dismiss(host = host, intentId = action.intentId)
		state
	}

	on<AssistantChat.Action.ToggleIntentDraft> { state, action ->
		machine.session.selectionByDraft[action.intentId to action.mutationId] = action.selected
		machine.refreshed(state)
	}
}

private fun ContentTransitions.contentObservationRows(
	machine: AssistantChatMachine,
	host: MachineHost<AssistantChat.Effect>
) {
	on<AssistantChatInternalEvent.ConversationObserved> { state, event ->
		machine.session.observe(event.conversation)
		machine.refreshed(state)
	}

	on<AssistantChatInternalEvent.AssistantOn> { state, event ->
		machine.session.guidedByMode = event.guided
		machine.refreshed(state)
	}

	on<AssistantChatInternalEvent.TurnAccepted> { state, event ->
		machine.turnAccepted(host = host, event = event)
		machine.refreshed(state)
	}

	on<AssistantChatInternalEvent.TurnRunFailed> { state, event ->
		machine.session.runFailed(texts = machine.texts, event = event)
		machine.refreshed(state)
	}

	on<AssistantChatInternalEvent.RestrictionChecked> { state, event ->
		machine.session.restriction = AssistantChatSession.Restriction(
			level = event.standing?.level,
			canAppeal = event.standing?.appealable == true
		)
		machine.refreshed(state)
	}
}

private fun ContentTransitions.contentOutcomeRows(
	machine: AssistantChatMachine,
	host: MachineHost<AssistantChat.Effect>
) {
	on<AssistantChatInternalEvent.SendRejected> { state, event ->
		machine.sendRejected(host = host, event = event)
		machine.refreshed(state)
	}

	onTo<AssistantChatInternalEvent.SendDisabled, AssistantChat.State.Unavailable>(
		emits = setOf(AssistantChat.Effect.Dismiss::class)
	) { _, _ ->
		host.sendEffect(AssistantChat.Effect.Dismiss)
		AssistantChat.State.Unavailable
	}

	onTo<AssistantChatInternalEvent.SendConsentRequired, AssistantChat.State.ConsentRequired> { _, _ ->
		AssistantChat.State.ConsentRequired
	}

	on<AssistantChatInternalEvent.IntentConfirmFinished> { state, event ->
		machine.session.runningIntents -= event.intentId
		machine.refreshed(state)
	}

	on<AssistantChatInternalEvent.EnrollmentProofDelegated>(
		emits = setOf(AssistantChat.Effect.OpenEnrollmentProof::class)
	) { state, event ->
		machine.session.runningIntents -= event.intentId
		host.sendEffect(AssistantChat.Effect.OpenEnrollmentProof)
		machine.refreshed(state)
	}

	on<AssistantChatInternalEvent.MessageRated>(emits = setOf(AssistantChat.Effect.ShowSnackBar::class)) { state, event ->
		host.sendEffect(AssistantChat.Effect.ShowSnackBar(message = event.message))
		state
	}

	on<AssistantChatInternalEvent.MessageReported>(emits = setOf(AssistantChat.Effect.ShowSnackBar::class)) {
		state, event ->
		host.sendEffect(AssistantChat.Effect.ShowSnackBar(message = event.message))
		state
	}

	on<AssistantChatInternalEvent.MessageReportRejected>(emits = setOf(AssistantChat.Effect.ShowSnackBar::class)) {
		state, event ->
		host.sendEffect(AssistantChat.Effect.ShowSnackBar(message = event.message))
		state
	}

	on<AssistantChatInternalEvent.AppealSent>(emits = setOf(AssistantChat.Effect.ShowSnackBar::class)) { state, event ->
		host.sendEffect(AssistantChat.Effect.ShowSnackBar(message = event.message))
		state
	}

	on<AssistantChatInternalEvent.AppealRejected>(emits = setOf(AssistantChat.Effect.ShowSnackBar::class)) {
		state, event ->
		host.sendEffect(AssistantChat.Effect.ShowSnackBar(message = event.message))
		state
	}
}
