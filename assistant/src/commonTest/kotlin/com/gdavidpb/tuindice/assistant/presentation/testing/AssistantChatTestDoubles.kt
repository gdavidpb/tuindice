package com.gdavidpb.tuindice.assistant.presentation.testing

import com.gdavidpb.tuindice.assistant.domain.model.AssistantActiveTurn
import com.gdavidpb.tuindice.assistant.domain.model.AssistantConversation
import com.gdavidpb.tuindice.assistant.domain.model.AssistantConversationItem
import com.gdavidpb.tuindice.assistant.domain.model.AssistantTurnFailure
import com.gdavidpb.tuindice.assistant.domain.model.AssistantTurnPhase
import com.gdavidpb.tuindice.assistant.domain.model.AssistantUserMessage
import com.gdavidpb.tuindice.assistant.domain.model.IntentDecisionResult
import com.gdavidpb.tuindice.assistant.domain.model.IntentResolvedCount
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantConversationRepository
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantIntentOutboxRepository
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantIntentRepository
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantMemoryRepository
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantServerModeRepository
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantStandingRepository
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantTurnRepository
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.model.AssistantReportReason
import com.gdavidpb.tuindice.assistant.presentation.model.ChatItem
import com.gdavidpb.tuindice.assistant.presentation.model.ChatPartItem
import com.gdavidpb.tuindice.assistant.presentation.model.IntentCardItem
import com.gdavidpb.tuindice.assistant.presentation.resource.AssistantChatFeedbackTexts
import com.gdavidpb.tuindice.assistant.presentation.resource.AssistantChatIntentTexts
import com.gdavidpb.tuindice.assistant.presentation.resource.AssistantChatPartTexts
import com.gdavidpb.tuindice.assistant.presentation.resource.AssistantChatTexts
import com.gdavidpb.tuindice.assistant.presentation.viewmodel.AssistantChatViewModel
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantAvailability
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantMode
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreen
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreenContext
import com.gdavidpb.tuindice.base.domain.repository.AssistantAvailabilityRepository
import com.gdavidpb.tuindice.chatcontract.abuse.AbuseAppeal
import com.gdavidpb.tuindice.chatcontract.component.NavTarget
import com.gdavidpb.tuindice.chatcontract.context.EntityRef
import com.gdavidpb.tuindice.chatcontract.event.AssistantEvent
import com.gdavidpb.tuindice.chatcontract.event.IntentProposed
import com.gdavidpb.tuindice.chatcontract.event.IntentResolved
import com.gdavidpb.tuindice.chatcontract.event.MessageCompleted
import com.gdavidpb.tuindice.chatcontract.event.Progress
import com.gdavidpb.tuindice.chatcontract.event.RunError
import com.gdavidpb.tuindice.chatcontract.event.RunErrorCode
import com.gdavidpb.tuindice.chatcontract.event.RunFinished
import com.gdavidpb.tuindice.chatcontract.event.RunStarted
import com.gdavidpb.tuindice.chatcontract.event.UnknownAssistantEvent
import com.gdavidpb.tuindice.chatcontract.intent.EvaluationDraft
import com.gdavidpb.tuindice.chatcontract.intent.Intent
import com.gdavidpb.tuindice.chatcontract.intent.IntentDecision
import com.gdavidpb.tuindice.chatcontract.intent.IntentOutcomeReport
import com.gdavidpb.tuindice.chatcontract.intent.OutcomeKind
import com.gdavidpb.tuindice.chatcontract.memory.MemoryList
import com.gdavidpb.tuindice.chatcontract.message.AssistantMessage
import com.gdavidpb.tuindice.chatcontract.message.MessagePart
import com.gdavidpb.tuindice.chatcontract.turn.TurnAccepted
import com.gdavidpb.tuindice.chatcontract.turn.TurnRequest
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update

internal object FakeAssistantChatPartTexts : AssistantChatPartTexts {
	override suspend fun partNotShown(): String = "part-not-shown"
}

internal object FakeAssistantChatIntentTexts : AssistantChatIntentTexts {
	override suspend fun confirmFailed(): String = "confirm-failed"

	override suspend fun dismissFailed(): String = "dismiss-failed"
}

// The texts are told apart by their values, so an assertion can say which notice the student sees.
internal class FakeAssistantChatTexts :
	AssistantChatTexts,
	AssistantChatPartTexts by FakeAssistantChatPartTexts,
	AssistantChatIntentTexts by FakeAssistantChatIntentTexts {
	override suspend fun screenName(screen: AssistantScreen): String = "screen:${screen.name}"

	override suspend fun evaluationTypeName(evaluationType: String): String = "type:$evaluationType"

	override suspend fun maxGrade(maxGrade: Double): String = "max:$maxGrade"

	override suspend fun undated(): String = "undated"

	override suspend fun guided(): String = "guided"

	override suspend fun quotaExhausted(retryAfterSeconds: Int?): String = "quota:$retryAfterSeconds"

	override suspend fun turnFailure(code: RunErrorCode, retryable: Boolean): String {
		return if (code == RunErrorCode.OTHER && !retryable) "failure:$code:final" else "failure:$code"
	}

	override suspend fun sendFailed(): String = "send-failed"

	override suspend fun intentConfirming(): String = "confirming"

	override suspend fun messageTooLong(maxLength: Int): String = "too-long:$maxLength"
}

internal class FakeAssistantChatFeedbackTexts : AssistantChatFeedbackTexts {
	override suspend fun rated(): String = "rated"

	override suspend fun rateFailed(): String = "rate-failed"

	override suspend fun stillAppliesFailed(): String = "still-applies-failed"

	override suspend fun reported(): String = "reported"

	override suspend fun reportFailed(): String = "report-failed"

	override suspend fun reportTooLong(maxLength: Int): String = "report-too-long:$maxLength"

	override suspend fun appealSent(): String = "appeal-sent"

	override suspend fun appealFailed(maxLength: Int): String = "appeal-failed:$maxLength"

	override suspend fun appealNotSent(): String = "appeal-not-sent"
}

// A conversation the test changes: the store's observation emits each change, as the store does.
internal class ObservedAssistantConversationRepository(
	initial: AssistantConversation = emptyAssistantConversation()
) : AssistantConversationRepository, AssistantIntentOutboxRepository {
	private val staged = linkedMapOf<String, IntentOutcomeReport>()

	val conversation = MutableStateFlow(initial)
	val applied = mutableListOf<AssistantEvent>()
	val unconfirmed = mutableListOf<String>()

	override fun observeConversation(): Flow<AssistantConversation> = conversation

	override fun currentConversation(): AssistantConversation = conversation.value

	// The session the conversation shows, as the store reports it: a test that moves it moves the marks the writes carry.
	override fun sessionMark(): Int = conversation.value.sessionMark

	override fun attachConversation(conversationId: String, sessionMark: Int) {
		conversation.update { current -> current.copy(conversationId = conversationId) }
	}

	// The events of the turns are written as the store writes them, so the sheet shows the conversation the turns made.
	override fun applyEvent(event: AssistantEvent, sessionMark: Int) {
		applied += event
		conversation.update { current -> current.reducedBy(event) }
	}

	override fun markTurnUnconfirmed(turnId: String, lastSeq: Long, sessionMark: Int) {
		unconfirmed += turnId
		conversation.update { current -> current.unconfirmedBy(turnId, lastSeq) }
	}

	// Idempotent by id, as the store is: a message that comes again, such as a retry's, is not written twice. A retry
	// takes the question it replaces out in the same write, as the store does, unless its session has ended. Each
	// replacement is recorded with the session it was decided in, so a test can say which session a retry took.
	val replacements = mutableListOf<Pair<String, Int>>()

	override fun addUserMessage(message: AssistantUserMessage, replacesMessageId: String?, replacesSessionMark: Int?) {
		val replaced = replacesMessageId?.takeIf { replacesSessionMark == sessionMark() }

		if (replacesMessageId != null && replacesSessionMark != null) {
			replacements += replacesMessageId to replacesSessionMark
		}

		conversation.update { current ->
			val kept = if (replaced == null) {
				current
			} else {
				current.copy(
					items = current.items.filterNot { item ->
						item is AssistantConversationItem.User && item.message.id == replaced
					}
				)
			}
			val written = kept.items.any { item -> item is AssistantConversationItem.User && item.message.id == message.id }

			if (written) kept else kept.copy(items = kept.items + AssistantConversationItem.User(message))
		}
	}

	override fun addAssistantMessage(message: AssistantMessage, intent: Intent?, sessionMark: Int) {
		conversation.update { current ->
			val items = current.items + AssistantConversationItem.Agent(message = message, turnId = "")
			val openIntents = intent?.let { current.openIntents + (it.id to it) } ?: current.openIntents

			current.copy(items = items, openIntents = openIntents)
		}
	}

	override fun stageOutcome(report: IntentOutcomeReport, sessionMark: Int) {
		staged[report.intentId] = report
	}

	// Clears the report only when it is still the one staged for its intent, as the store does.
	override fun clearOutcome(report: IntentOutcomeReport) {
		if (staged[report.intentId] == report) staged.remove(report.intentId)
	}

	override fun pendingOutcomes(): List<IntentOutcomeReport> = staged.values.toList()

	// The intents whose confirmation runs, claimed as the store claims them: a second confirmation of one is refused.
	private val confirming = mutableSetOf<String>()

	override fun claimConfirmation(intentId: String, sessionMark: Int): Boolean {
		val claimable = sessionMark == this.sessionMark() &&
			intentId in conversation.value.openIntents &&
			intentId !in confirming

		if (claimable) confirming += intentId

		return claimable
	}

	override fun releaseConfirmation(intentId: String, sessionMark: Int) {
		if (sessionMark == this.sessionMark()) confirming -= intentId
	}

	override fun markIntentResolved(
		intentId: String,
		outcome: OutcomeKind,
		sessionMark: Int,
		count: IntentResolvedCount?,
		detailCode: String?
	) {
		conversation.update { current ->
			current.copy(
				openIntents = current.openIntents - intentId,
				resolvedIntents = current.resolvedIntents + (intentId to outcome),
				resolvedCounts = if (count == null) {
					current.resolvedCounts - intentId
				} else {
					current.resolvedCounts + (intentId to count)
				},
				resolvedDetails = if (detailCode == null) {
					current.resolvedDetails - intentId
				} else {
					current.resolvedDetails + (intentId to detailCode)
				}
			)
		}
	}

	override suspend fun clearConversation() {
		conversation.value = emptyAssistantConversation()
	}
}

// Like the real availability, the current value reads as off until something observes it: the configuration has no
// synchronous read, so a sheet that read it before observing would close while the assistant is on.
internal class ObservedAssistantAvailabilityRepository(
	initial: AssistantAvailability = AssistantAvailability(mode = AssistantMode.ON, serverDisabled = false)
) : AssistantAvailabilityRepository, AssistantServerModeRepository {
	val availability = MutableStateFlow(initial)
	private var observed = false

	override fun sessionMark(): Int = 0

	// The mode the server reports with the status decides over the configured one.
	override fun reportServerMode(mode: AssistantMode, sessionMark: Int) {
		availability.update { current -> current.copy(mode = mode) }
	}

	override fun observeAvailability(): Flow<AssistantAvailability> = availability.onStart { observed = true }

	override fun currentAvailability(): AssistantAvailability =
		if (observed) availability.value else AssistantAvailability(mode = AssistantMode.OFF, serverDisabled = false)
}

internal fun emptyAssistantConversation(): AssistantConversation {
	return AssistantConversation(
		conversationId = null,
		items = emptyList(),
		openIntents = emptyMap(),
		resolvedIntents = emptyMap(),
		activeTurn = null
	)
}

// The store's reduction of an event over the last turn it saw: an event at or below the seq of its turn is already in,
// and a turn the conversation has not seen becomes the last turn.
internal fun AssistantConversation.reducedBy(event: AssistantEvent): AssistantConversation {
	val known = activeTurn?.takeIf { turn -> turn.turnId == event.turnId }
	val applied = event !is UnknownAssistantEvent && (known == null || event.seq > known.lastSeq)

	if (!applied) return this

	val turn = (known ?: AssistantActiveTurn(event.turnId, event.seq, null, AssistantTurnPhase.RUNNING))
		.afterEvent(event)
	val withTurn = copy(activeTurn = turn)

	return when (event) {
		is MessageCompleted -> withTurn.copy(items = items.withAgentMessage(event.message, event.turnId))

		is IntentProposed -> withTurn.copy(openIntents = openIntents + (event.intent.id to event.intent))

		// An intent the conversation already resolved keeps the outcome it got.
		is IntentResolved -> withTurn.copy(
			openIntents = openIntents - event.intentId,
			resolvedIntents = if (event.intentId in resolvedIntents) {
				resolvedIntents
			} else {
				resolvedIntents + (event.intentId to event.outcome)
			}
		)

		else -> withTurn
	}
}

// How a turn stands after [event]: an unconfirmed turn that delivers an event which is not its final one runs again.
private fun AssistantActiveTurn.afterEvent(event: AssistantEvent): AssistantActiveTurn {
	val isFinal = event is RunFinished || event is RunError
	val resumed = if (status == AssistantTurnPhase.UNCONFIRMED && !isFinal) {
		copy(status = AssistantTurnPhase.RUNNING)
	} else {
		this
	}

	val changed = when (event) {
		is RunStarted -> resumed.copy(progressLabel = null, status = AssistantTurnPhase.RUNNING, failure = null)

		is Progress -> resumed.copy(progressLabel = event.label)

		is RunFinished -> resumed.copy(progressLabel = null, status = AssistantTurnPhase.FINISHED)

		is RunError -> resumed.copy(
			progressLabel = null,
			status = AssistantTurnPhase.FAILED,
			failure = AssistantTurnFailure(
				code = event.code,
				retryable = event.retryable,
				retryAfterSeconds = event.retryAfterSeconds
			)
		)

		else -> resumed
	}

	return changed.copy(lastSeq = event.seq)
}

// The result of a turn the store did not see end is unconfirmed; a turn that is not running keeps its end.
internal fun AssistantConversation.unconfirmedBy(turnId: String, lastSeq: Long): AssistantConversation {
	val turn = activeTurn?.takeIf { active -> active.turnId == turnId }

	return when {
		turn == null -> copy(
			activeTurn = AssistantActiveTurn(turnId, lastSeq, null, AssistantTurnPhase.UNCONFIRMED)
		)

		turn.status == AssistantTurnPhase.RUNNING -> copy(
			activeTurn = turn.copy(progressLabel = null, status = AssistantTurnPhase.UNCONFIRMED)
		)

		else -> this
	}
}

// A message that comes again replaces the one it has, in the place it has.
private fun List<AssistantConversationItem>.withAgentMessage(
	message: AssistantMessage,
	turnId: String
): List<AssistantConversationItem> {
	val item = AssistantConversationItem.Agent(message = message, turnId = turnId)
	val index = indexOfFirst { existing ->
		existing is AssistantConversationItem.Agent && existing.message.id == message.id
	}

	return if (index < 0) this + item else toMutableList().also { items -> items[index] = item }
}

internal fun agentMessage(id: String, vararg parts: MessagePart): AssistantConversationItem {
	return AssistantConversationItem.Agent(
		message = AssistantMessage(id = id, parts = parts.toList(), createdAtMillis = 0),
		turnId = "t-$id"
	)
}

// The decision waits for the test's go-ahead while [gated]: the confirmation then stays running until the test says so.
internal class GatedAssistantIntentRepository(
	private val delegate: AssistantIntentRepository
) : AssistantIntentRepository {
	var gated = false
	val release = CompletableDeferred<Unit>()

	override suspend fun decide(decision: IntentDecision): IntentDecisionResult {
		if (gated) release.await()

		return delegate.decide(decision)
	}

	override suspend fun reportOutcome(report: IntentOutcomeReport) {
		delegate.reportOutcome(report)
	}
}

// The appeal fails with the failure a test sets, as the service can refuse it; the standing is the delegate's.
internal class FailingAppealStandingRepository(
	private val delegate: AssistantStandingRepository
) : AssistantStandingRepository by delegate {
	var appealFailure: Throwable? = null

	override suspend fun appeal(appeal: AbuseAppeal) {
		appealFailure?.let { failure -> throw failure }

		delegate.appeal(appeal)
	}
}

// The send waits for the test's go-ahead while [gated]: the request stays on its way until the test releases it. The
// rest of the turn repository is the delegate's.
internal class GatedAssistantTurnRepository(
	private val delegate: AssistantTurnRepository
) : AssistantTurnRepository by delegate {
	var gated = false
	val release = CompletableDeferred<Unit>()

	override suspend fun sendTurn(request: TurnRequest): TurnAccepted {
		if (gated) release.await()

		return delegate.sendTurn(request)
	}
}

// The answer about a memory waits for the test's go-ahead while [gated]: the save stays running until the test says so.
// The next read of the list [holdNextList] keeps the list it read, and returns when the test says so.
internal class GatedAssistantMemoryRepository(
	private val delegate: AssistantMemoryRepository
) : AssistantMemoryRepository by delegate {
	var gated = false
	val release = CompletableDeferred<Unit>()
	var holdNextList = false
	val releaseHeldList = CompletableDeferred<Unit>()

	// The failure the held read breaks with when the test releases it, so the read fails after it was asked.
	var heldListError: Throwable? = null

	override suspend fun listMemories(): MemoryList {
		val list = delegate.listMemories()

		if (holdNextList) {
			holdNextList = false
			releaseHeldList.await()
			heldListError?.let { error -> throw error }
		}

		return list
	}

	override suspend fun answerStillApplies(memoryId: String, applies: Boolean) {
		if (gated) release.await()

		delegate.answerStillApplies(memoryId, applies)
	}
}

internal fun assistantConversation(
	items: List<AssistantConversationItem> = emptyList(),
	openIntents: Map<String, Intent> = emptyMap(),
	activeTurn: AssistantActiveTurn? = null,
	resolvedIntents: Map<String, OutcomeKind> = emptyMap(),
	resolvedCounts: Map<String, IntentResolvedCount> = emptyMap()
): AssistantConversation {
	return AssistantConversation(
		conversationId = null,
		items = items,
		openIntents = openIntents,
		resolvedIntents = resolvedIntents,
		resolvedCounts = resolvedCounts,
		activeTurn = activeTurn
	)
}

internal fun assistantDraft(mutationId: String, reference: String): EvaluationDraft {
	return EvaluationDraft(
		mutationId = mutationId,
		reference = reference,
		attemptId = "attempt-1",
		subjectCode = "CI4325",
		termId = "2026-2",
		evaluationType = "QUIZ",
		scheduleMode = "dated",
		maxGrade = 20.0
	)
}

internal fun AssistantChatViewModel.content(): AssistantChat.State.Content = state.value as AssistantChat.State.Content

internal fun AssistantChat.State.Content.bubbles(): List<ChatItem.User> = items.filterIsInstance<ChatItem.User>()

internal fun AssistantChat.State.Content.intentCard(intentId: String): IntentCardItem {
	return items
		.filterIsInstance<ChatItem.Agent>()
		.flatMap { agent -> agent.parts }
		.filterIsInstance<ChatPartItem.IntentCard>()
		.single { part -> part.card.intentId == intentId }
		.card
}

// The actions of the sheet, named as the screen calls them.
internal fun AssistantChatViewModel.openAction(context: AssistantScreenContext?) =
	dispatch(AssistantChat.Action.Open(context))

internal fun AssistantChatViewModel.changeTextAction(text: String) =
	dispatch(AssistantChat.Action.ChangeText(text))

internal fun AssistantChatViewModel.sendMessageAction() =
	dispatch(AssistantChat.Action.Send)

internal fun AssistantChatViewModel.pickSuggestionAction(suggestionId: String) =
	dispatch(AssistantChat.Action.PickSuggestion(suggestionId))

internal fun AssistantChatViewModel.removeContextAction() =
	dispatch(AssistantChat.Action.RemoveContext)

internal fun AssistantChatViewModel.retryTurnAction() =
	dispatch(AssistantChat.Action.RetryTurn)

internal fun AssistantChatViewModel.cancelTurnAction() =
	dispatch(AssistantChat.Action.CancelTurn)

internal fun AssistantChatViewModel.confirmIntentAction(intentId: String) =
	dispatch(AssistantChat.Action.ConfirmIntent(intentId))

internal fun AssistantChatViewModel.dismissIntentAction(intentId: String) =
	dispatch(AssistantChat.Action.DismissIntent(intentId))

internal fun AssistantChatViewModel.toggleIntentDraftAction(intentId: String, mutationId: String, selected: Boolean) =
	dispatch(AssistantChat.Action.ToggleIntentDraft(intentId, mutationId, selected))

internal fun AssistantChatViewModel.copyMessageAction(messageId: String) =
	dispatch(AssistantChat.Action.CopyMessage(messageId))

internal fun AssistantChatViewModel.rateMessageAction(messageId: String, useful: Boolean) =
	dispatch(AssistantChat.Action.RateMessage(messageId, useful))

internal fun AssistantChatViewModel.reportMessageAction(
	messageId: String,
	reason: AssistantReportReason,
	comment: String?
) =
	dispatch(AssistantChat.Action.ReportMessage(messageId, reason, comment))

internal fun AssistantChatViewModel.openNavTargetAction(target: NavTarget, entityRefs: List<EntityRef>) =
	dispatch(AssistantChat.Action.OpenNavTarget(target, entityRefs))

internal fun AssistantChatViewModel.openComponentSheetAction(messageId: String, componentId: String) =
	dispatch(AssistantChat.Action.OpenComponentSheet(messageId, componentId))

internal fun AssistantChatViewModel.answerStillAppliesAction(memoryId: String, applies: Boolean) =
	dispatch(AssistantChat.Action.AnswerStillApplies(memoryId, applies))

internal fun AssistantChatViewModel.appealAction(text: String) =
	dispatch(AssistantChat.Action.Appeal(text))

internal fun AssistantChatViewModel.acceptConsentAction() =
	dispatch(AssistantChat.Action.AcceptConsent)

internal fun AssistantChatViewModel.declineConsentAction() =
	dispatch(AssistantChat.Action.DeclineConsent)

internal fun AssistantChatViewModel.openPrivacyPolicyAction() =
	dispatch(AssistantChat.Action.OpenPrivacyPolicy)
