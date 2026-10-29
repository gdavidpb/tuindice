package com.gdavidpb.tuindice.data.source.assistant

import com.gdavidpb.tuindice.academiccore.domain.model.Evaluation
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantEvaluationCardRepository
import com.gdavidpb.tuindice.base.ui.model.EvaluationCardItem
import com.gdavidpb.tuindice.evaluations.domain.mapper.sortedAsListed
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationDisplayContext
import com.gdavidpb.tuindice.evaluations.domain.repository.EvaluationRepository
import com.gdavidpb.tuindice.evaluations.presentation.mapper.EvaluationItemMapping
import com.gdavidpb.tuindice.evaluations.presentation.mapper.getEvaluationItemMapping
import com.gdavidpb.tuindice.evaluations.presentation.mapper.toEvaluationCardItems
import kotlinx.coroutines.flow.first
import kotlin.time.Clock

// Reads the evaluations the device has, with the term and the subjects the Evaluations tab lists them under, and draws
// their cards with the tab's own mapping, so the assistant never writes a state, a date or a grade by itself.
class EvaluationCardsAssistantDataSource(
	private val evaluationRepository: EvaluationRepository,
	private val clock: Clock
) : AssistantEvaluationCardRepository {
	override suspend fun evaluationCards(evaluationIds: Set<String>): Map<String, EvaluationCardItem> {
		if (evaluationIds.isEmpty()) return emptyMap()

		return evaluationCardsOf(
			evaluationIds = evaluationIds,
			evaluations = evaluationRepository.getEvaluationsSnapshot().value,
			displayContext = evaluationRepository.observeDisplayContextFlow().first(),
			mapping = getEvaluationItemMapping(),
			nowMillis = clock.now().toEpochMilliseconds()
		)
	}
}

// The cards of [evaluationIds] among [evaluations], as the Evaluations tab draws them at [nowMillis]: the evaluations
// go in the tab's order and through the tab's groups, so each card has the name, the date, the place, the grade and
// the state the tab shows. An id the device does not have, or one the tab does not list, has no card.
internal fun evaluationCardsOf(
	evaluationIds: Set<String>,
	evaluations: List<Evaluation>,
	displayContext: EvaluationDisplayContext,
	mapping: EvaluationItemMapping,
	nowMillis: Long
): Map<String, EvaluationCardItem> {
	return evaluations
		.sortedAsListed(nowMillis = nowMillis)
		.toEvaluationCardItems(
			currentTerm = displayContext.currentTerm,
			attempts = displayContext.attempts,
			mapping = mapping
		)
		.filterKeys { id -> id in evaluationIds }
}
