package com.gdavidpb.tuindice.evaluations.ui.model

import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreenContext
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsWeekKey

// What the evaluations do when they are tapped: adding one, a week, an evaluation's grade, its edit and its delete, a
// retry, and the arrival taken from a jump. [onAskAssistant] asks the assistant about an evaluation, and is null where
// the assistant cannot be reached: the evaluations then offer no question.
data class EvaluationsActions(
	val onAddEvaluationClick: () -> Unit = {},
	val onWeekClick: (EvaluationsWeekKey) -> Unit = {},
	val onEvaluationClick: (evaluationId: String, evaluationName: String, subjectCode: String) -> Unit = { _, _, _ -> },
	val onEvaluationEdit: (evaluationId: String) -> Unit = {},
	val onEvaluationDelete: (evaluationId: String) -> Unit = {},
	val onEvaluationArrivalTaken: (evaluationId: String) -> Unit = {},
	val onRetryClick: () -> Unit = {},
	val onAskAssistant: ((context: AssistantScreenContext) -> Unit)? = null
)
