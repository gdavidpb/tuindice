package com.gdavidpb.tuindice.assistant.presentation.machine

import com.gdavidpb.tuindice.assistant.domain.model.AssistantInputLimits
import com.gdavidpb.tuindice.assistant.domain.usecase.error.SendAssistantMessageError
import com.gdavidpb.tuindice.assistant.domain.usecase.param.ConfirmIntentParams
import com.gdavidpb.tuindice.assistant.domain.usecase.param.SendAssistantMessageParams
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.mapper.AssistantChatTexts
import com.gdavidpb.tuindice.assistant.presentation.mapper.toChatContextItem
import com.gdavidpb.tuindice.assistant.presentation.model.ChatComposerItem
import com.gdavidpb.tuindice.assistant.presentation.transition.assistantChatAnyStateTransitions
import com.gdavidpb.tuindice.assistant.presentation.transition.assistantChatConsentRequiredTransitions
import com.gdavidpb.tuindice.assistant.presentation.transition.assistantChatContentTransitions
import com.gdavidpb.tuindice.assistant.presentation.transition.assistantChatIdleTransitions
import com.gdavidpb.tuindice.assistant.presentation.transition.assistantChatUnavailableTransitions
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantMode
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreenContext
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinition
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost
import com.gdavidpb.tuindice.base.presentation.statemachine.ScreenMachine
import com.gdavidpb.tuindice.chatcontract.intent.AddEvaluationsArgs
import com.gdavidpb.tuindice.chatcontract.intent.OutcomeKind
import com.gdavidpb.tuindice.chatcontract.intent.RequestEnrollmentProofArgs
import kotlinx.coroutines.flow.collect
import kotlin.uuid.Uuid

/**
 * The chat sheet: its states, its transitions and the commands behind them. The conversation is observed from the
 * store; the machine holds what the student did on the sheet in [session] and the text being written in [draft].
 */
class AssistantChatMachine(
	internal val turns: AssistantChatTurns,
	internal val intents: AssistantChatIntents,
	internal val planIntents: AssistantChatPlanIntents,
	internal val feedback: AssistantChatFeedback,
	internal val access: AssistantChatAccess,
	internal val texts: AssistantChatTexts
) : ScreenMachine<AssistantChat.State, AssistantChat.Effect> {
	internal val session = AssistantChatSession()

	internal val draft = AssistantChatDraft()

	override fun initialState(): AssistantChat.State = AssistantChat.State.Idle

	override fun define(host: MachineHost<AssistantChat.Effect>): MachineDefinition<AssistantChat.State> {
		return MachineDefinition.define {
			assistantChatIdleTransitions(machine = this@AssistantChatMachine, host = host)
			assistantChatConsentRequiredTransitions(machine = this@AssistantChatMachine, host = host)
			assistantChatUnavailableTransitions()
			assistantChatContentTransitions(machine = this@AssistantChatMachine, host = host)
			assistantChatAnyStateTransitions(machine = this@AssistantChatMachine, host = host)
		}
	}

	// The status and the availability are read before the decision, and the availability is observed before its value is
	// read, so a change in between is not lost. The decision re-enters the table from Idle, where each outcome has its own
	// target.
	internal fun openSheet(host: MachineHost<AssistantChat.Effect>, context: AssistantScreenContext?) {
		session.openedContext = context

		// The opening waits for the first value the availability emits: until something observes it, its current value
		// reads as off, so reading it right away would close a sheet that the assistant is on for. The changes after
		// that first value are followed from then on.
		host.launchMachineJob {
			// The status goes first, so the availability read after it carries the server's mode.
			access.applyStatusWithin(STATUS_OPEN_TIMEOUT_MILLIS)

			val availability = access.firstAvailability()
			session.guidedByMode = availability.mode == AssistantMode.GUIDED

			host.processInternalEvent(
				when {
					!availability.isVisible -> AssistantChatInternalEvent.OpeningUnavailable
					!access.isConsentGranted() -> AssistantChatInternalEvent.OpeningNeedsConsent
					else -> AssistantChatInternalEvent.OpeningReady
				}
			)
			access.observeAvailability(host)
		}
	}

	// The content of an open sheet: the conversation starts being observed, and a turn that was running resumes.
	internal suspend fun enterContent(host: MachineHost<AssistantChat.Effect>): AssistantChat.State.Content {
		turns.observeConversation(host)
		turns.resumeTurn(host)
		intents.flushOutcomes(host)

		val emptyContent = AssistantChat.State.Content(
			context = session.openedContext?.toChatContextItem(texts),
			items = emptyList(),
			progressLabel = null,
			composer = ChatComposerItem(text = "", isEnabled = false, isSendEnabled = false),
			suggestions = emptyList(),
			notice = null,
			isGuided = false
		)

		return refreshed(emptyContent)
	}

	// Leaving the content stops what observed it: the conversation and the turn are no longer followed, and a send that
	// was waiting for its first event is no longer waited for.
	internal fun leaveContent() {
		turns.stop()

		session.sending = false
		session.sendingTurnId = null
	}

	internal suspend fun refreshed(content: AssistantChat.State.Content): AssistantChat.State.Content {
		return session.presentedContent(texts = texts, draftText = draft.text.value, content = content)
	}

	// Starts a turn with [text]. [draftText] is the draft the send takes its text from and clears; null leaves it. A new
	// message gets its own client turn id; a retry passes the one of the send it retries, so the server answers the same turn.
	internal fun startTurn(
		host: MachineHost<AssistantChat.Effect>,
		text: String,
		suggestionId: String?,
		context: AssistantScreenContext?,
		draftText: String?,
		clientTurnId: String = Uuid.random().toString()
	) {
		session.turnFailure = null
		session.sending = true
		session.sendingTurnId = null

		if (draftText != null) draft.setText("")

		turns.send(
			host = host,
			params = SendAssistantMessageParams(
				text = text,
				suggestionId = suggestionId,
				screenContext = context,
				clientTurnId = clientTurnId
			),
			sentText = text,
			restoreText = draftText,
			suggestionId = suggestionId
		)
	}

	// Confirms the open intent [intentId]. Its outcome comes back as an event; the card shows it is running meanwhile.
	internal fun confirmIntent(host: MachineHost<AssistantChat.Effect>, intentId: String) {
		val intent = session.openIntent(intentId) ?: return

		val batch = intent.args as? AddEvaluationsArgs
		val editedArgs = batch?.copy(
			items = batch.items.map { item ->
				item.copy(selected = session.selectionOf(intentId, item.mutationId) ?: item.selected)
			}
		)

		val params = ConfirmIntentParams(intentId = intentId, editedArgs = editedArgs)
		val flow = intents.confirmation(intent.args, params) ?: planIntents.confirmation(intent.args, params) ?: return

		session.runningIntents += intentId

		host.launchMachineJob {
			flow.collect { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> Unit

					is UseCaseState.Data -> host.processInternalEvent(
						// Only a delegated proof opens its flow; an expired or conflicting one shows its outcome on the card.
						if (intent.args is RequestEnrollmentProofArgs && useCaseState.value == OutcomeKind.DELEGATED) {
							AssistantChatInternalEvent.EnrollmentProofDelegated(intentId = intentId)
						} else {
							AssistantChatInternalEvent.IntentConfirmFinished(intentId = intentId)
						}
					)

					is UseCaseState.Error -> host.processInternalEvent(
						AssistantChatInternalEvent.IntentConfirmFinished(intentId = intentId)
					)
				}
			}
		}
	}

	// A send that did not start: the student's message reads as failed, and the notice says what stopped it.
	internal suspend fun sendRejected(
		host: MachineHost<AssistantChat.Effect>,
		event: AssistantChatInternalEvent.SendRejected
	) {
		val error = event.error
		val notSent = error is SendAssistantMessageError.InvalidMessage

		session.sending = false

		if (!notSent) {
			session.markSendFailed(event.sentText)
			session.lastFailedSend = AssistantChatSession.FailedSend(
				text = event.sentText,
				suggestionId = event.suggestionId,
				clientTurnId = event.clientTurnId
			)
		}

		// A text that was not sent stays in the composer, where the student can send it again.
		val keepsText = notSent ||
			error is SendAssistantMessageError.QuotaExhausted ||
			error == SendAssistantMessageError.TurnInProgress ||
			error == SendAssistantMessageError.GuidedOnly

		if (keepsText) event.restoreText?.let(draft::setText)

		when (error) {
			is SendAssistantMessageError.QuotaExhausted -> {
				session.quota = AssistantChatSession.Quota(retryAfterSeconds = error.retryAfterSeconds)
			}

			SendAssistantMessageError.Restricted -> {
				session.restriction = AssistantChatSession.Restriction(level = null, canAppeal = false)
				access.restrictionStatus(host)
			}

			SendAssistantMessageError.GuidedOnly -> {
				session.guidedByServer = true
				access.refreshStatus(host)
			}

			SendAssistantMessageError.TurnInProgress -> Unit

			SendAssistantMessageError.InvalidMessage -> {
				session.turnFailure = AssistantChatSession.TurnFailure(
					message = texts.messageTooLong(AssistantInputLimits.MESSAGE_MAX_LENGTH),
					retryable = false
				)
			}

			else -> {
				session.turnFailure = AssistantChatSession.TurnFailure(
					message = texts.sendFailed(),
					retryable = true
				)
			}
		}
	}

	// The turn was accepted: whatever failed before is past, and the turn is followed from its first event.
	internal fun turnAccepted(
		host: MachineHost<AssistantChat.Effect>,
		event: AssistantChatInternalEvent.TurnAccepted
	) {
		// The retry was accepted: its message is no longer failed, and the turn is followed from its first event.
		session.lastFailedSend?.let { failed -> session.failedMessages -= failed.clientTurnId }
		session.lastFailedSend = null
		session.turnFailure = null
		session.quota = null
		session.restriction = null
		session.pendingFailedText = null
		session.followedSuggestionId = event.suggestionId
		session.sendingTurnId = event.turnId

		turns.followTurn(host = host, turnId = event.turnId, afterSeq = event.lastSeq)
	}
}

// How long opening the sheet waits for the status the server gives the student.
private const val STATUS_OPEN_TIMEOUT_MILLIS = 3_000L
