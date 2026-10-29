package com.gdavidpb.tuindice.assistant.testing

import com.gdavidpb.tuindice.assistant.domain.repository.AssistantEvaluationCardRepository
import com.gdavidpb.tuindice.base.ui.model.EvaluationCardItem

// The cards of the Evaluations tab the tests read: none unless a test gives some, which is a device that has none of
// the evaluations the chat names. The ids each read asks for are kept, in order.
internal class FakeAssistantEvaluationCardRepository(
	private val cards: Map<String, EvaluationCardItem> = emptyMap()
) : AssistantEvaluationCardRepository {
	val reads = mutableListOf<Set<String>>()

	override suspend fun evaluationCards(evaluationIds: Set<String>): Map<String, EvaluationCardItem> {
		reads += evaluationIds

		return cards.filterKeys { id -> id in evaluationIds }
	}
}
