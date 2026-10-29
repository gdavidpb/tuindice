package com.gdavidpb.tuindice.summary.testing

import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantEntityKind
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantEntityRef
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantInsightAction
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantInsightSection
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantNavTarget
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantTermInsight
import com.gdavidpb.tuindice.base.domain.repository.AssistantInsightsRepository
import com.gdavidpb.tuindice.summary.domain.repository.TermReportFeedbackRepository
import com.gdavidpb.tuindice.summary.domain.repository.TermReportSeenRepository
import com.gdavidpb.tuindice.summary.domain.usecase.ObserveTermReportEntryUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.SummaryTermReports
import com.gdavidpb.tuindice.summary.domain.usecase.UpdateTermReportsUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.UpdateTermReportsExceptionHandler
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

// The reports the service holds for the account. [refreshFailure] makes asking for them fail, the way an offline device
// does.
class ControllableAssistantInsightsRepository(
	initialInsights: List<AssistantTermInsight> = emptyList(),
	var refreshFailure: Throwable? = null,
	var ratingFailure: Throwable? = null
) : AssistantInsightsRepository {
	val insightsFlow = MutableStateFlow(initialInsights)

	val ratings = mutableListOf<Pair<String, Boolean>>()

	var refreshCount = 0
		private set

	override fun observeInsightsFlow(): Flow<List<AssistantTermInsight>> = insightsFlow

	override suspend fun refreshInsights() {
		refreshCount++

		refreshFailure?.let { throwable -> throw throwable }
	}

	override suspend fun rateInsight(insightId: String, useful: Boolean) {
		ratingFailure?.let { throwable -> throw throwable }

		ratings += insightId to useful
	}
}

// The ids of the reports opened on the device, as the test sets them.
class ControllableTermReportSeenRepository(
	initialSeenIds: Set<String> = emptySet()
) : TermReportSeenRepository {
	val seenIdsFlow = MutableStateFlow(initialSeenIds)

	override fun observeSeenTermReportIds(): Flow<Set<String>> = seenIdsFlow

	override suspend fun markTermReportSeen(insightId: String) {
		seenIdsFlow.value = seenIdsFlow.value + insightId
	}
}

// The thumbs the student's votes on the device, by insight id, as the test sets them.
class ControllableTermReportFeedbackRepository(
	initialFeedbacks: Map<String, Boolean> = emptyMap()
) : TermReportFeedbackRepository {
	val feedbacksFlow = MutableStateFlow(initialFeedbacks)

	override fun observeTermReportFeedbacks(): Flow<Map<String, Boolean>> = feedbacksFlow

	override suspend fun saveTermReportFeedback(insightId: String, useful: Boolean) {
		feedbacksFlow.value = feedbacksFlow.value + (insightId to useful)
	}
}

// The service answers each thumb when the test says so: a request waits until it is answered, so that taps can overlap.
class ManualRatingsInsightsRepository(
	initialInsights: List<AssistantTermInsight> = emptyList()
) : AssistantInsightsRepository {
	val insightsFlow = MutableStateFlow(initialInsights)

	val requests = mutableListOf<PendingThumb>()

	// How many thumbs the service has been asked for, so that a test can wait for the next one to be sent.
	val requestCount = MutableStateFlow(0)

	override fun observeInsightsFlow(): Flow<List<AssistantTermInsight>> = insightsFlow

	override suspend fun refreshInsights() = Unit

	override suspend fun rateInsight(insightId: String, useful: Boolean) {
		val request = PendingThumb(useful = useful)

		requests += request
		requestCount.value = requests.size

		request.answer.await()
	}
}

// One thumb sent to the service, and the answer the test gives it.
class PendingThumb(
	val useful: Boolean
) {
	internal val answer = CompletableDeferred<Unit>()

	fun accept() {
		answer.complete(Unit)
	}

	fun refuse() {
		answer.completeExceptionally(IllegalStateException("404"))
	}
}

// A report of the term [termKey], written at [createdAtMillis]. Its share action names the term "term-1".
fun termReportInsight(
	id: String = "term-1:2",
	termKey: String = "2026-APR_JUL",
	createdAtMillis: Long = 1_761_000_000_000L,
	sections: List<AssistantInsightSection> = listOf(
		AssistantInsightSection(title = "Cerraste con 4,18", text = "Aprobaste las 3 materias."),
		AssistantInsightSection(title = "Tu ritmo", text = "Este trimestre aprobaste 11 créditos.")
	),
	nextActions: List<AssistantInsightAction> = listOf(
		AssistantInsightAction(target = AssistantNavTarget.RECORD_PROJECTION, label = "Proyectar tu índice"),
		AssistantInsightAction(
			target = AssistantNavTarget.SHARE_TERM,
			label = "Compartir tu trimestre",
			entityRefs = listOf(AssistantEntityRef(kind = AssistantEntityKind.TERM, id = "term-1"))
		)
	)
) = AssistantTermInsight(
	id = id,
	termKey = termKey,
	kind = "term_end",
	title = "Cerraste con 4,18",
	body = sections.joinToString(separator = "\n\n") { section -> section.text },
	entityRefs = listOf(AssistantEntityRef(kind = AssistantEntityKind.TERM, id = "term-1")),
	sections = sections,
	nextActions = nextActions,
	createdAtMillis = createdAtMillis
)

// The use case that offers the line of the newest unopened report, over the reports and the ids the test sets.
fun createObserveTermReportEntryUseCase(
	insights: AssistantInsightsRepository = ControllableAssistantInsightsRepository(),
	seen: TermReportSeenRepository = ControllableTermReportSeenRepository()
) = ObserveTermReportEntryUseCase(
	insightsRepository = insights,
	seenRepository = seen,
	reportingRepository = RecordingReportingRepository()
)

fun createUpdateTermReportsUseCase(
	insights: AssistantInsightsRepository = ControllableAssistantInsightsRepository()
) = UpdateTermReportsUseCase(
	insightsRepository = insights,
	reportingRepository = RecordingReportingRepository(),
	exceptionHandler = UpdateTermReportsExceptionHandler()
)

fun createSummaryTermReports(
	insights: AssistantInsightsRepository = ControllableAssistantInsightsRepository(),
	seen: TermReportSeenRepository = ControllableTermReportSeenRepository()
) = SummaryTermReports(
	entry = createObserveTermReportEntryUseCase(insights = insights, seen = seen),
	refresh = createUpdateTermReportsUseCase(insights = insights)
)
