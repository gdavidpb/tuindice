package com.gdavidpb.tuindice.assistant.domain.repository

import com.gdavidpb.tuindice.base.ui.model.EvaluationCardItem

// Implemented by maincore, which holds the evaluations of the device and the mapping the Evaluations tab draws them
// with. The answer has the card the tab shows for each of [evaluationIds] the device has, by id, and nothing for one it
// does not have: the assistant then draws that one from what the chat carries.
interface AssistantEvaluationCardRepository {
	suspend fun evaluationCards(evaluationIds: Set<String>): Map<String, EvaluationCardItem>
}
