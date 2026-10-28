package com.gdavidpb.tuindice.assistant.presentation.transition

import com.gdavidpb.tuindice.assistant.domain.model.AssistantConversationItem
import com.gdavidpb.tuindice.assistant.domain.usecase.error.ExtractEvaluationsFromPhotoError
import com.gdavidpb.tuindice.assistant.domain.usecase.param.SendAssistantMessageParams
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.machine.AssistantChatInternalEvent
import com.gdavidpb.tuindice.assistant.presentation.machine.AssistantChatMachine
import com.gdavidpb.tuindice.assistant.presentation.machine.AssistantChatSession
import com.gdavidpb.tuindice.assistant.presentation.machine.awaitsMemoryRead
import com.gdavidpb.tuindice.assistant.presentation.machine.memoryAnswer
import com.gdavidpb.tuindice.assistant.presentation.machine.nextMemoryRead
import com.gdavidpb.tuindice.assistant.presentation.machine.retryFailedSend
import com.gdavidpb.tuindice.assistant.presentation.machine.sendIsBusy
import com.gdavidpb.tuindice.assistant.presentation.machine.sendNewMessage
import com.gdavidpb.tuindice.assistant.presentation.machine.turnNotRecovered
import com.gdavidpb.tuindice.assistant.presentation.machine.turnRecovered
import com.gdavidpb.tuindice.assistant.presentation.mapper.presentedComposer
import com.gdavidpb.tuindice.assistant.presentation.mapper.toWire
import com.gdavidpb.tuindice.assistant.presentation.model.ChatItem
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinitionBuilder
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost
import com.gdavidpb.tuindice.chatcontract.component.StillAppliesComponent
import com.gdavidpb.tuindice.chatcontract.message.Component

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
		contentSendRows(machine, host)
		contentPhotoRows(machine, host)
		contentMessageRows(machine, host)
		contentIntentRows(machine, host)

		// A link of a card that leaves the app: the system opens it, as it opens the privacy policy.
		on<AssistantChat.Action.OpenExternalLink>(
			emits = setOf(AssistantChat.Effect.OpenUrl::class)
		) { state, action ->
			host.sendEffect(AssistantChat.Effect.OpenUrl(url = action.url))
			state
		}

		contentObservationRows(machine, host)
		contentConsentRows()

		// The outcomes of the jobs the content started are the ones the consent takes too (see the outcome rows), and each
		// one is presented here.
		sendOutcomeRows(machine = machine, host = host) { state -> machine.refreshed(state) }
		intentOutcomeRows(machine = machine, host = host) { state -> machine.refreshed(state) }
		feedbackOutcomeRows(machine = machine, host = host) { state -> machine.refreshed(state) }
		stillAppliesOutcomeRows(machine = machine, host = host) { state -> machine.refreshed(state) }
		contentNotificationOfferRows(machine, host)
	}
}

private fun ContentTransitions.contentComposerRows(
	machine: AssistantChatMachine,
	host: MachineHost<AssistantChat.Effect>
) {
	on<AssistantChat.Action.ChangeText> { state, action ->
		if (!state.composer.isEnabled) return@on state

		machine.draft.setText(action.text)
		// Only the text moved: the conversation is the same, so its items keep their instance and only the composer is
		// presented again. The agent's messages are not composed again while the student types.
		val restricted = machine.session.restrictionAt(machine.access.restrictions.clock.now().toEpochMilliseconds())

		state.copy(
			composer = machine.session.presentedComposer(machine.draft.text.value, machine.draft.revision, restricted)
		)
	}

	on<AssistantChat.Action.RemoveContext> { state, _ -> machine.refreshed(state.copy(context = null)) }

	// The student stops the turn that runs or is being sent. A turn that has no stop target has nothing to stop.
	on<AssistantChat.Action.CancelTurn> { state, _ ->
		val turnId = machine.session.stopTarget() ?: return@on state

		machine.turns.cancel(host = host, turnId = turnId)
		machine.refreshed(state)
	}
}

// The rows that start a text turn: the send, a suggestion and a retry. They wait for the same conditions: no message on
// its way, no turn running, and no photo being read.
private fun ContentTransitions.contentSendRows(
	machine: AssistantChatMachine,
	host: MachineHost<AssistantChat.Effect>
) {
	on<AssistantChat.Action.Send> { state, _ ->
		if (!state.composer.isSendEnabled) return@on state

		val draftText = machine.draft.text.value

		machine.sendNewMessage(
			host = host,
			params = SendAssistantMessageParams(
				text = draftText.trim(),
				suggestionId = null,
				screenContext = state.context?.context
			),
			draftText = draftText
		)
		machine.refreshed(state)
	}

	on<AssistantChat.Action.PickSuggestion> { state, action ->
		val text = machine.session.suggestionText(action.suggestionId)

		if (text == null || machine.session.sendIsBusy()) return@on state

		machine.sendNewMessage(
			host = host,
			params = SendAssistantMessageParams(
				text = text,
				suggestionId = action.suggestionId,
				screenContext = state.context?.context
			),
			draftText = machine.draft.text.value
		)
		machine.refreshed(state)
	}

	on<AssistantChat.Action.RetryTurn> { state, _ ->
		val failed = machine.session.lastFailedSend

		if (failed == null || machine.session.sendIsBusy()) return@on state

		machine.retryFailedSend(host = host, failed = failed, context = state.context?.context)
		machine.refreshed(state)
	}

	// The answer to a message that was unconfirmed is recovered first: the message that waits for it is sent once
	// the answer is confirmed, and is not sent when it is not. The recovery is stopped with the content, so its
	// outcome comes here only.
	on<AssistantChatInternalEvent.TurnRecovered> { state, event ->
		machine.turnRecovered(host = host, event = event)
		machine.refreshed(state)
	}

	on<AssistantChatInternalEvent.TurnNotRecovered> { state, event ->
		machine.turnNotRecovered(event = event)
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

		machine.session.photoFailure = null
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

// A photo that was not read. Its notices are its own: the quota, the guided mode, a restriction, or a failure the
// student can see. A photo has no text to send again, so its failure is never retryable. It never marks the failure
// of a text turn: the failure of a text turn keeps its notice and its retry.
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
			session.restriction = AssistantChatSession.Restriction(level = null, untilMillis = null, canAppeal = false)
			access.restrictions.restrictionStatus(host)
		}

		// As a refused text send does, the status is read again: the service says the chat is guided now.
		ExtractEvaluationsFromPhotoError.GuidedOnly -> {
			session.guidedByServer = true
			access.refreshStatus(host)
		}

		else -> session.photoFailure = AssistantChatSession.TurnFailure(
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
		val previous = machine.session.usefulByMessage[action.messageId]

		machine.session.usefulByMessage[action.messageId] = action.useful
		machine.feedback.rate(host = host, messageId = action.messageId, useful = action.useful, previous = previous)
		machine.refreshed(state)
	}

	// A message is reported once: the service keeps one report per message, so a reported one asks for no reason again.
	on<AssistantChat.Action.ReportMessage> { state, action ->
		if (action.messageId in machine.session.reportedMessages) return@on state

		machine.feedback.report(
			host = host,
			messageId = action.messageId,
			reason = action.reason.toWire(),
			comment = action.comment
		)
		state
	}

	// An answer already saved, still being saved, or waiting for the read that covers its card is not sent again: a second
	// tap on the same memory does nothing.
	on<AssistantChat.Action.AnswerStillApplies> { state, action ->
		val session = machine.session
		val waiting = action.memoryId in session.pendingMemories || session.awaitsMemoryRead(action.memoryId)

		if (waiting || session.memoryAnswer(action.memoryId) != null) return@on state

		session.pendingMemories += action.memoryId
		machine.feedback.answerStillApplies(host = host, memoryId = action.memoryId, applies = action.applies)
		machine.refreshed(state)
	}

	on<AssistantChat.Action.Appeal> { state, action ->
		machine.access.restrictions.appeal(host = host, text = action.text)
		state
	}

	// The host's jump closes the sheet in the same change of the stack (maincore's goFromAssistant). A Dismiss after it
	// would close whatever the jump left on top: the tab it landed on, back to the start tab, or the screen it opened.
	on<AssistantChat.Action.OpenNavTarget>(
		emits = setOf(AssistantChat.Effect.NavigateTo::class)
	) { state, action ->
		host.sendEffect(AssistantChat.Effect.NavigateTo(target = action.target, entityRefs = action.entityRefs))
		state
	}

	// The consent embedded in a message links the privacy policy too; it opens outside the app, as in the consent.
	on<AssistantChat.Action.OpenPrivacyPolicy>(
		emits = setOf(AssistantChat.Effect.OpenUrl::class)
	) { state, _ ->
		machine.access.openPrivacyPolicy(host = host)
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
		machine.proposals.intents.dismiss(host = host, intentId = action.intentId)
		state
	}

	on<AssistantChat.Action.ToggleIntentDraft> { state, action ->
		machine.session.selectionByDraft[action.intentId to action.mutationId] = action.selected
		machine.refreshed(state)
	}
}

// The card of the notification permission. The platform's answer decides whether it shows, once per device: the mark is
// written when the card shows. Both of its buttons hide it for the rest of the sheet; «Activar avisos» also asks the
// system for the permission.
private fun ContentTransitions.contentNotificationOfferRows(
	machine: AssistantChatMachine,
	host: MachineHost<AssistantChat.Effect>
) {
	on<AssistantChatInternalEvent.NotificationOfferChecked> { state, event ->
		if (event.needed && state.items.isEmpty() && !machine.session.sending) {
			machine.session.notificationOfferShown = true
			machine.notificationOffer.markOffered()
		}

		machine.refreshed(state)
	}

	on<AssistantChat.Action.ActivateNotifications>(
		emits = setOf(AssistantChat.Effect.RequestNotificationPermission::class)
	) { state, _ ->
		if (!state.notificationOffer) return@on state

		machine.session.notificationOfferShown = false
		host.sendEffect(AssistantChat.Effect.RequestNotificationPermission)
		machine.refreshed(state)
	}

	on<AssistantChat.Action.DismissNotificationOffer> { state, _ ->
		if (!state.notificationOffer) return@on state

		machine.session.notificationOfferShown = false
		machine.refreshed(state)
	}
}

// The conversation the store observes while the content is shown, and the access to it: these rows exist on the content
// only, since the conversation is not observed while the sheet asks for the consent.
private fun ContentTransitions.contentObservationRows(
	machine: AssistantChatMachine,
	host: MachineHost<AssistantChat.Effect>
) {
	on<AssistantChatInternalEvent.ConversationObserved> { state, event ->
		machine.session.observe(event.conversation)

		// One read for each new «¿Sigue aplicando?» card, whatever the state of its memory: a card whose component id no
		// read has covered yet asks for a read, which covers the memories of those cards. While the cards do not change,
		// no read is asked again, and a failed read is not asked again until a new card comes.
		val session = machine.session
		val cards = session.conversation?.items.orEmpty()
			.filterIsInstance<AssistantConversationItem.Agent>()
			.flatMap { item -> item.message.parts }
			.filterIsInstance<Component>()
			.mapNotNull { part ->
				(part.component as? StillAppliesComponent)?.let { card -> card.id to card.memoryId }
			}

		val uncovered = cards.filter { (componentId, _) -> componentId !in session.memoryReadCards }

		if (uncovered.isNotEmpty()) {
			val read = session.nextMemoryRead()

			session.memoryReadCards += cards.map { (componentId, _) -> componentId }
			uncovered.forEach { (_, memoryId) -> session.coveringReads[memoryId] = read }
			machine.feedback.loadMemoryStates(host, read = read)
		}

		machine.refreshed(state)
	}

	on<AssistantChatInternalEvent.AssistantOn> { state, event ->
		machine.session.modeSaid(guided = event.guided)
		machine.refreshed(state)
	}
}

// A send the service answers with a consent asks the student for it: the sheet shows the consent.
private fun ContentTransitions.contentConsentRows() {
	onTo<AssistantChatInternalEvent.SendConsentRequired, AssistantChat.State.ConsentRequired> { _, _ ->
		AssistantChat.State.ConsentRequired
	}
}
