package com.gdavidpb.tuindice.summary.testing

import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantEntityKind
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantEntityRef
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantInsightAction
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantInsightSection
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantNavTarget
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantTermInsight
import com.gdavidpb.tuindice.base.domain.repository.AssistantInsightsRepository
import com.gdavidpb.tuindice.summary.domain.repository.TermReportSeenRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

// The reports the service holds for the account. [refreshFailure] makes asking for them fail, the way an offline device does.
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

// A report of the term [termKey] of the term [termId], written at [createdAtMillis].
fun termReportInsight(
	id: String = "term-1:2",
	termKey: String = "2026-APR_JUL",
	termId: String = "term-1",
	title: String = "Cerraste con 4,18",
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
			entityRefs = listOf(AssistantEntityRef(kind = AssistantEntityKind.TERM, id = termId))
		)
	)
) = AssistantTermInsight(
	id = id,
	termKey = termKey,
	kind = "term_end",
	title = title,
	body = sections.joinToString(separator = "\n\n") { section -> section.text },
	entityRefs = listOf(AssistantEntityRef(kind = AssistantEntityKind.TERM, id = termId)),
	sections = sections,
	nextActions = nextActions,
	createdAtMillis = createdAtMillis
)
