package com.gdavidpb.tuindice.assistant.presentation.testing

import com.gdavidpb.tuindice.assistant.domain.model.AssistantActiveTurn
import com.gdavidpb.tuindice.assistant.domain.model.AssistantConversation
import com.gdavidpb.tuindice.assistant.domain.model.AssistantConversationItem
import com.gdavidpb.tuindice.assistant.domain.model.AssistantUserMessage
import com.gdavidpb.tuindice.assistant.domain.model.IntentDecisionResult
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantConversationRepository
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantIntentOutboxRepository
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantIntentRepository
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantMemoryRepository
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantServerModeRepository
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.model.AssistantReportReason
import com.gdavidpb.tuindice.assistant.presentation.model.ChatItem
import com.gdavidpb.tuindice.assistant.presentation.model.ChatPartItem
import com.gdavidpb.tuindice.assistant.presentation.model.IntentCardItem
import com.gdavidpb.tuindice.assistant.presentation.resource.AssistantChatFeedbackTexts
import com.gdavidpb.tuindice.assistant.presentation.resource.AssistantChatTexts
import com.gdavidpb.tuindice.assistant.presentation.viewmodel.AssistantChatViewModel
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantAvailability
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantMode
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreen
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreenContext
import com.gdavidpb.tuindice.base.domain.repository.AssistantAvailabilityRepository
import com.gdavidpb.tuindice.chatcontract.component.NavTarget
import com.gdavidpb.tuindice.chatcontract.context.EntityRef
import com.gdavidpb.tuindice.chatcontract.event.AssistantEvent
import com.gdavidpb.tuindice.chatcontract.event.RunErrorCode
import com.gdavidpb.tuindice.chatcontract.intent.EvaluationDraft
import com.gdavidpb.tuindice.chatcontract.intent.Intent
import com.gdavidpb.tuindice.chatcontract.intent.IntentDecision
import com.gdavidpb.tuindice.chatcontract.intent.IntentOutcomeReport
import com.gdavidpb.tuindice.chatcontract.intent.OutcomeKind
import com.gdavidpb.tuindice.chatcontract.memory.MemoryList
import com.gdavidpb.tuindice.chatcontract.message.AssistantMessage
import com.gdavidpb.tuindice.chatcontract.message.MessagePart
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update

// The texts are told apart by their values, so an assertion can say which notice the student sees.
internal class FakeAssistantChatTexts : AssistantChatTexts {
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

	override fun sessionMark(): Int = 0

	override fun attachConversation(conversationId: String, sessionMark: Int) {
		conversation.update { current -> current.copy(conversationId = conversationId) }
	}

	override fun applyEvent(event: AssistantEvent, sessionMark: Int) {
		applied += event
	}

	override fun markTurnUnconfirmed(turnId: String, lastSeq: Long, sessionMark: Int) {
		unconfirmed += turnId
	}

	// Idempotent by id, as the store is: a message that comes again, such as a retry's, is not written twice.
	override fun addUserMessage(message: AssistantUserMessage) {
		conversation.update { current ->
			val written = current.items.any { item -> item is AssistantConversationItem.User && item.message.id == message.id }

			if (written) current else current.copy(items = current.items + AssistantConversationItem.User(message))
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

	override fun clearOutcome(intentId: String) {
		staged.remove(intentId)
	}

	override fun pendingOutcomes(): List<IntentOutcomeReport> = staged.values.toList()

	override fun markIntentResolved(intentId: String, outcome: OutcomeKind, sessionMark: Int) {
		conversation.update { current ->
			current.copy(
				openIntents = current.openIntents - intentId,
				resolvedIntents = current.resolvedIntents + (intentId to outcome)
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

// The answer about a memory waits for the test's go-ahead while [gated]: the save stays running until the test says so.
// The next read of the list [holdNextList] keeps the list it read, and returns when the test says so.
internal class GatedAssistantMemoryRepository(
	private val delegate: AssistantMemoryRepository
) : AssistantMemoryRepository by delegate {
	var gated = false
	val release = CompletableDeferred<Unit>()
	var holdNextList = false
	val releaseHeldList = CompletableDeferred<Unit>()

	override suspend fun listMemories(): MemoryList {
		val list = delegate.listMemories()

		if (holdNextList) {
			holdNextList = false
			releaseHeldList.await()
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
	activeTurn: AssistantActiveTurn? = null
): AssistantConversation {
	return AssistantConversation(
		conversationId = null,
		items = items,
		openIntents = openIntents,
		resolvedIntents = emptyMap(),
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
