package com.gdavidpb.tuindice.pensum.ui.model

import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreenContext
import com.gdavidpb.tuindice.pensum.presentation.model.PensumModalityItem
import com.gdavidpb.tuindice.pensum.presentation.model.PensumOptionItem

// What the pensum does when it is tapped: a retry, the summary, the pensum's own selection, a subject's statistics,
// and the ask about a subject. [onAskAssistant] is null where the assistant cannot be reached: the subjects then offer
// no question.
data class PensumActions(
	val onRetryClick: () -> Unit = {},
	val onSummaryCollapsedToggle: () -> Unit = {},
	val onSelectionSheetDismiss: () -> Unit = {},
	val onSubjectStatsClick: (subjectCode: String) -> Unit = {},
	val onSelectionApplied: (PensumOptionItem, PensumModalityItem) -> Unit = { _, _ -> },
	val onPensumContextClick: () -> Unit = {},
	val onAskAssistant: ((context: AssistantScreenContext) -> Unit)? = null
)
