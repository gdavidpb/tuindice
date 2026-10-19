package com.gdavidpb.tuindice.record.ui.model

import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreenContext

// What the record does when it is tapped: a term, an attempt's grade or outcome, a retry, the actions of the synthetic
// terms, the enrollment proof and the closing of the term selection. [onAskAssistant] asks the assistant about an
// attempt, and is null where the assistant cannot be reached: the attempts then offer no question.
data class RecordActions(
	val onSelectedTermChange: (termId: String) -> Unit = {},
	val onRetryClick: () -> Unit = {},
	val onAttemptArrivalTaken: (attemptId: String) -> Unit = {},
	val onAttemptSelectionChange: (
		attemptId: String,
		newGrade: Int?,
		newOutcome: AttemptOutcome?,
		isSelected: Boolean
	) -> Unit = { _, _, _, _ -> },
	val onCreateSyntheticTermClick: () -> Unit = {},
	val onUpdateSyntheticTermClick: (termId: String) -> Unit = {},
	val onDeleteSyntheticTermClick: (termId: String) -> Unit = {},
	val onEnrollmentProofClick: () -> Unit = {},
	val onDismissTermSelection: () -> Unit = {},
	val onAskAssistant: ((context: AssistantScreenContext) -> Unit)? = null
)
