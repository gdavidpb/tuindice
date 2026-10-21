package com.gdavidpb.tuindice.assistant.testing

import com.gdavidpb.tuindice.assistant.domain.model.AssistantActiveTurn
import com.gdavidpb.tuindice.assistant.domain.model.AssistantConversation
import com.gdavidpb.tuindice.assistant.domain.model.AssistantUserMessage
import com.gdavidpb.tuindice.assistant.domain.model.IntentDecisionResult
import com.gdavidpb.tuindice.assistant.domain.model.IntentExecution
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantClientStateRepository
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantConversationRepository
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantEvaluationsRepository
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantExtractionRepository
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantFeedbackRepository
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantIntentOutboxRepository
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantIntentRepository
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantMemoryRepository
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantPlansRepository
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantProjectionRepository
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantQueuedMutationsRepository
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantServerConsentRepository
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantServerDisabledRepository
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantStandingRepository
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantTurnRepository
import com.gdavidpb.tuindice.base.domain.model.EncodedImage
import com.gdavidpb.tuindice.base.domain.model.ImageEncodingSpec
import com.gdavidpb.tuindice.base.domain.repository.AssistantConsentRepository
import com.gdavidpb.tuindice.base.domain.repository.ImageEncoderRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.chatcontract.abuse.AbuseAppeal
import com.gdavidpb.tuindice.chatcontract.abuse.AbuseStanding
import com.gdavidpb.tuindice.chatcontract.abuse.StandingLevel
import com.gdavidpb.tuindice.chatcontract.consent.ConsentStatus
import com.gdavidpb.tuindice.chatcontract.context.ClientState
import com.gdavidpb.tuindice.chatcontract.event.AssistantEvent
import com.gdavidpb.tuindice.chatcontract.event.UsageSummary
import com.gdavidpb.tuindice.chatcontract.extraction.ExtractionRequest
import com.gdavidpb.tuindice.chatcontract.extraction.ExtractionResult
import com.gdavidpb.tuindice.chatcontract.feedback.MessageFeedback
import com.gdavidpb.tuindice.chatcontract.feedback.MessageReport
import com.gdavidpb.tuindice.chatcontract.intent.AddEvaluationsArgs
import com.gdavidpb.tuindice.chatcontract.intent.CreatePlanArgs
import com.gdavidpb.tuindice.chatcontract.intent.EditEvaluationArgs
import com.gdavidpb.tuindice.chatcontract.intent.EditPlanArgs
import com.gdavidpb.tuindice.chatcontract.intent.Intent
import com.gdavidpb.tuindice.chatcontract.intent.IntentArgs
import com.gdavidpb.tuindice.chatcontract.intent.IntentBinding
import com.gdavidpb.tuindice.chatcontract.intent.IntentDecision
import com.gdavidpb.tuindice.chatcontract.intent.IntentOutcomeReport
import com.gdavidpb.tuindice.chatcontract.intent.IntentRisk
import com.gdavidpb.tuindice.chatcontract.intent.MutationOutcome
import com.gdavidpb.tuindice.chatcontract.intent.OutcomeKind
import com.gdavidpb.tuindice.chatcontract.intent.ProjectionArgs
import com.gdavidpb.tuindice.chatcontract.intent.RemoveEvaluationArgs
import com.gdavidpb.tuindice.chatcontract.intent.RemovePlanArgs
import com.gdavidpb.tuindice.chatcontract.intent.UnknownIntentArgs
import com.gdavidpb.tuindice.chatcontract.memory.MemoryList
import com.gdavidpb.tuindice.chatcontract.message.AssistantMessage
import com.gdavidpb.tuindice.chatcontract.turn.AssistantStatus
import com.gdavidpb.tuindice.chatcontract.turn.TurnAccepted
import com.gdavidpb.tuindice.chatcontract.turn.TurnRequest
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlin.time.Clock
import kotlin.time.Instant

// The fakes record what they are asked, and the calls they take go into a log shared by the test, so a test can check
// the order of the calls across ports.

internal class RecordingAssistantTurnRepository(
	private val calls: MutableList<String> = mutableListOf()
) : AssistantTurnRepository {
	var acceptedTurn = TurnAccepted(turnId = "t1", conversationId = "c1", lastSeq = 0)
	var sendError: Throwable? = null
	var events: List<AssistantEvent> = emptyList()

	// The events of the next follows, one list per follow, when a test scripts them; [events] answers the others.
	val scriptedFollows = mutableListOf<List<AssistantEvent>>()

	// The failure every follow breaks with, when a test makes the stream break.
	var streamError: Throwable? = null

	// When set, a follow delivers nothing and stays open, as a stream waiting for its first event does.
	var holdEvents = false
	val requests = mutableListOf<TurnRequest>()
	val cancelled = mutableListOf<String>()
	val followed = mutableListOf<Pair<String, Long>>()
	var conversationsDeleted = 0

	override suspend fun sendTurn(request: TurnRequest): TurnAccepted {
		calls += "sendTurn"
		requests += request
		sendError?.let { error -> throw error }

		return acceptedTurn
	}

	override suspend fun cancelTurn(turnId: String) {
		calls += "cancelTurn"
		cancelled += turnId
	}

	override fun turnEvents(turnId: String, afterSeq: Long): Flow<AssistantEvent> {
		followed += turnId to afterSeq

		val answer = scriptedFollows.removeFirstOrNull() ?: events

		return flow {
			streamError?.let { error -> throw error }

			if (holdEvents) awaitCancellation()

			emitAll(answer.asFlow())
		}
	}

	override suspend fun deleteConversations() {
		calls += "deleteConversations"
		conversationsDeleted++
	}
}

internal class RecordingAssistantConversationRepository(
	private val calls: MutableList<String> = mutableListOf(),
	var conversationId: String? = null,
	var openIntents: Map<String, Intent> = emptyMap(),
	var activeTurn: AssistantActiveTurn? = null
) : AssistantConversationRepository, AssistantIntentOutboxRepository {
	val attached = mutableListOf<String>()
	val applied = mutableListOf<AssistantEvent>()
	val unconfirmed = mutableListOf<String>()
	val userMessages = mutableListOf<AssistantUserMessage>()
	val resolved = mutableMapOf<String, OutcomeKind>()
	val assistantMessages = mutableListOf<Pair<AssistantMessage, Intent?>>()

	override fun observeConversation(): Flow<AssistantConversation> = flowOf(currentConversation())

	override fun currentConversation(): AssistantConversation {
		return AssistantConversation(
			conversationId = conversationId,
			items = emptyList(),
			openIntents = openIntents,
			resolvedIntents = resolved.toMap(),
			activeTurn = activeTurn
		)
	}

	override fun sessionMark(): Int = 0

	override fun attachConversation(conversationId: String, sessionMark: Int) {
		calls += "attachConversation"
		attached += conversationId
		this.conversationId = conversationId
	}

	override fun applyEvent(event: AssistantEvent, sessionMark: Int) {
		applied += event
	}

	override fun markTurnUnconfirmed(turnId: String, lastSeq: Long, sessionMark: Int) {
		calls += "markTurnUnconfirmed"
		unconfirmed += turnId
	}

	override fun addUserMessage(message: AssistantUserMessage) {
		calls += "addUserMessage"
		userMessages += message
	}

	override fun addAssistantMessage(message: AssistantMessage, intent: Intent?, sessionMark: Int) {
		calls += "addAssistantMessage"
		assistantMessages += message to intent
	}

	override fun markIntentResolved(intentId: String, outcome: OutcomeKind, sessionMark: Int) {
		calls += "markIntentResolved"
		resolved[intentId] = outcome
		openIntents = openIntents - intentId
	}

	private val staged = linkedMapOf<String, IntentOutcomeReport>()

	override fun stageOutcome(report: IntentOutcomeReport, sessionMark: Int) {
		staged[report.intentId] = report
	}

	override fun clearOutcome(intentId: String) {
		staged.remove(intentId)
	}

	override fun pendingOutcomes(): List<IntentOutcomeReport> = staged.values.toList()

	var cleared = 0

	override suspend fun clearConversation() {
		calls += "clearConversation"
		cleared++
	}
}

internal class RecordingAssistantIntentRepository(
	private val calls: MutableList<String> = mutableListOf()
) : AssistantIntentRepository {
	var answer: IntentDecisionResult = IntentDecisionResult.Accepted
	var decideError: Throwable? = null
	var reportError: Throwable? = null
	val decisions = mutableListOf<IntentDecision>()
	val reports = mutableListOf<IntentOutcomeReport>()

	override suspend fun decide(decision: IntentDecision): IntentDecisionResult {
		calls += "decide"
		decisions += decision
		decideError?.let { error -> throw error }

		return answer
	}

	// A report the server does not take is not recorded as sent.
	override suspend fun reportOutcome(report: IntentOutcomeReport) {
		calls += "reportOutcome"
		reportError?.let { error -> throw error }

		reports += report
	}
}

internal class RecordingAssistantFeedbackRepository : AssistantFeedbackRepository {
	val rated = mutableListOf<MessageFeedback>()
	val reported = mutableListOf<MessageReport>()

	// The failure every rating breaks with, when a test makes the server refuse it.
	var rateError: Throwable? = null

	override suspend fun rate(feedback: MessageFeedback) {
		rateError?.let { error -> throw error }

		rated += feedback
	}

	// The failure every report breaks with, when a test makes the server refuse it. A refused report is not kept.
	var reportError: Throwable? = null

	override suspend fun report(report: MessageReport) {
		reportError?.let { error -> throw error }

		reported += report
	}
}

internal class RecordingAssistantMemoryRepository : AssistantMemoryRepository {
	var memories = MemoryList(items = emptyList())
	val deleted = mutableListOf<String>()
	var allDeleted = 0
	val answers = mutableListOf<Pair<String, Boolean>>()

	// The failure every answer breaks with, when a test makes the server refuse it.
	var answerError: Throwable? = null

	// How many times the list was read, and the failure every read breaks with, when a test makes the server refuse it.
	var listCalls = 0
	var listError: Throwable? = null

	override suspend fun listMemories(): MemoryList {
		listCalls++
		listError?.let { error -> throw error }

		return memories
	}

	override suspend fun deleteMemory(memoryId: String) {
		deleted += memoryId
	}

	override suspend fun deleteAllMemories() {
		allDeleted++
	}

	override suspend fun answerStillApplies(memoryId: String, applies: Boolean) {
		answerError?.let { error -> throw error }

		answers += memoryId to applies
	}
}

// [configuredMode] and [consentGranted] are what the status reads until a test sets an answer: the server agrees with
// the configuration and with the consent then.
internal class RecordingAssistantStandingRepository(
	private val configuredMode: () -> String = { "on" },
	private val consentGranted: () -> Boolean = { true }
) : AssistantStandingRepository {
	private var explicit = false

	var answer: AssistantStatus = assistantStatus()
		set(value) {
			field = value
			explicit = true
		}

	var failure: Throwable? = null
	val appeals = mutableListOf<AbuseAppeal>()

	// How many times the status was read, failed reads included.
	var statusReads = 0
		private set

	override suspend fun status(): AssistantStatus {
		statusReads++
		failure?.let { error -> throw error }

		if (explicit) return answer

		return answer.copy(
			mode = configuredMode(),
			consent = ConsentStatus(version = answer.consent.version, accepted = consentGranted())
		)
	}

	override suspend fun appeal(appeal: AbuseAppeal) {
		appeals += appeal
	}
}

internal class RecordingAssistantServerDisabledRepository : AssistantServerDisabledRepository {
	val marks = mutableListOf<Int?>()

	override fun sessionMark(): Int = 0

	override fun markServerDisabled(retryAfterSeconds: Int?, sessionMark: Int) {
		marks += retryAfterSeconds
	}
}

internal class FakeAssistantClientStateRepository(
	private val state: ClientState = ClientState(recordRevision = 3)
) : AssistantClientStateRepository {
	override suspend fun clientState(): ClientState = state
}

// The maincore ports answer FAILED instead of throwing, so a test sets the answer it wants.
internal class RecordingAssistantEvaluationsRepository(
	private val calls: MutableList<String> = mutableListOf()
) : AssistantEvaluationsRepository {
	var answer = IntentExecution(outcome = OutcomeKind.CONFIRMED)
	var error: Throwable? = null
	val received = mutableListOf<Any>()
	val expectedRevisions = mutableListOf<Long?>()

	override suspend fun add(args: AddEvaluationsArgs): IntentExecution = record(args)

	override suspend fun edit(args: EditEvaluationArgs, expectedRevision: Long?): IntentExecution {
		expectedRevisions += expectedRevision
		return record(args)
	}

	override suspend fun remove(args: RemoveEvaluationArgs, expectedRevision: Long?): IntentExecution {
		expectedRevisions += expectedRevision
		return record(args)
	}

	private fun record(args: Any): IntentExecution {
		calls += "port"
		received += args
		error?.let { failure -> throw failure }

		return answer
	}
}

// The follow-up of queued mutations, driven by the test: it keeps what it was asked to follow, and [settle] hands the
// outcomes to the intent that asked, as the session would once the mutations settle.
internal class RecordingAssistantQueuedMutationsRepository(
	private val calls: MutableList<String> = mutableListOf()
) : AssistantQueuedMutationsRepository {
	val followed = mutableListOf<Pair<List<String>, Long>>()
	private val waiting = mutableListOf<suspend (List<MutationOutcome>) -> Unit>()

	override fun followQueued(
		mutationIds: List<String>,
		untilMillis: Long,
		onSettled: suspend (List<MutationOutcome>) -> Unit
	) {
		calls += "followQueued"
		followed += mutationIds to untilMillis
		waiting += onSettled
	}

	suspend fun settle(outcomes: List<MutationOutcome>) {
		waiting.removeAt(0).invoke(outcomes)
	}
}

internal class RecordingAssistantPlansRepository(
	private val calls: MutableList<String> = mutableListOf()
) : AssistantPlansRepository {
	var answer = IntentExecution(outcome = OutcomeKind.CONFIRMED)
	val received = mutableListOf<Pair<Any, Long?>>()

	override suspend fun create(args: CreatePlanArgs, expectedRevision: Long?): IntentExecution {
		return record(args, expectedRevision)
	}

	override suspend fun edit(args: EditPlanArgs, expectedRevision: Long?): IntentExecution {
		return record(args, expectedRevision)
	}

	override suspend fun remove(args: RemovePlanArgs, expectedRevision: Long?): IntentExecution {
		return record(args, expectedRevision)
	}

	private fun record(args: Any, expectedRevision: Long?): IntentExecution {
		calls += "port"
		received += args to expectedRevision

		return answer
	}
}

internal class RecordingAssistantProjectionRepository(
	private val calls: MutableList<String> = mutableListOf()
) : AssistantProjectionRepository {
	var answer = IntentExecution(outcome = OutcomeKind.CONFIRMED)
	val received = mutableListOf<Pair<ProjectionArgs, Long?>>()

	override suspend fun save(args: ProjectionArgs, expectedRevision: Long?): IntentExecution {
		calls += "port"
		received += args to expectedRevision

		return answer
	}
}

internal class RecordingAssistantConsentRepository : AssistantConsentRepository, AssistantServerConsentRepository {
	private val consent = MutableStateFlow(false)
	val changes = mutableListOf<Boolean>()
	val reconciled = mutableListOf<ConsentStatus>()

	// The failure every change breaks with, when a test makes the server refuse it.
	var failure: Throwable? = null

	override val assistantConsentGranted: StateFlow<Boolean> = consent.asStateFlow()

	override fun isAssistantConsentGranted(): Boolean = consent.value

	override suspend fun setAssistantConsentGranted(granted: Boolean) {
		failure?.let { error -> throw error }

		changes += granted
		consent.value = granted
	}

	override suspend fun refreshAssistantConsent() = Unit

	override fun consentSessionMark(): Int = 0

	override suspend fun reconcileConsent(status: ConsentStatus, consentSessionMark: Int) {
		reconciled += status
		consent.value = status.accepted
	}
}

// The reading service of the photos: its answer, the requests it took, and a gate that holds the answer while a test
// looks at the chat in the middle of a reading.
internal class RecordingAssistantExtractionRepository : AssistantExtractionRepository {
	var result = ExtractionResult(message = "Encontré 1 evaluación.")
	var failure: Throwable? = null
	var gate: CompletableDeferred<Unit>? = null
	val requests = mutableListOf<ExtractionRequest>()

	override suspend fun extract(request: ExtractionRequest): ExtractionResult {
		requests += request
		gate?.await()
		failure?.let { error -> throw error }

		return result
	}
}

// The encoder of the photos: the bytes a photo is encoded to, and the specs it was asked for.
internal class RecordingImageEncoderRepository : ImageEncoderRepository {
	var content = byteArrayOf(1, 2, 3)
	val specs = mutableListOf<ImageEncodingSpec>()

	override suspend fun encode(file: PlatformFile, spec: ImageEncodingSpec): EncodedImage {
		specs += spec

		return EncodedImage(content = content, mimeType = "image/jpeg")
	}
}

// Only the instants the test sets, so the deadlines of the intents are decided in the test.
internal class FixedClock(var nowMillis: Long) : Clock {
	override fun now(): Instant = Instant.fromEpochMilliseconds(nowMillis)
}

internal const val FAR_FUTURE_MILLIS = 4_102_444_800_000L

internal fun assistantIntent(
	id: String = "i1",
	args: IntentArgs = UnknownIntentArgs,
	mutationIds: List<String> = listOf("m1"),
	expectedRevision: Long? = 7L,
	expiresAtMillis: Long = FAR_FUTURE_MILLIS
): Intent {
	return Intent(
		id = id,
		args = args,
		title = "Confirmar acción",
		effect = emptyList(),
		risk = IntentRisk.NORMAL,
		binding = IntentBinding(mutationIds = mutationIds, expectedRevision = expectedRevision, argsHash = "hash"),
		expiresAtMillis = expiresAtMillis,
		confirmLabel = "Confirmar",
		cancelLabel = "Cancelar"
	)
}

internal fun assistantStatus(): AssistantStatus {
	return AssistantStatus(
		mode = "on",
		consent = ConsentStatus(version = 1, accepted = true),
		standing = AbuseStanding(level = StandingLevel.NONE),
		usage = UsageSummary(remainingHour = 4, remainingDay = 20, remainingMonth = 300)
	)
}

// The values of the successful states of a use case, in order.
internal suspend fun Flow<UseCaseState<*, *>>.dataValues(): List<Any?> {
	return toList().filterIsInstance<UseCaseState.Data<*>>().map { state -> state.value }
}

internal suspend fun Flow<UseCaseState<*, *>>.lastState(): UseCaseState<*, *> = toList().last()
