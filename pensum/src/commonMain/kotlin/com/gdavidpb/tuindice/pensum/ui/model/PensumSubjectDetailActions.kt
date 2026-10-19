package com.gdavidpb.tuindice.pensum.ui.model

import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreenContext

// What the detail sheet of a subject does: its statistics, a related subject, its dismissal, and the ask about the
// subject. [onAskAssistant] is null where the assistant cannot be reached: the sheet then offers no question.
data class PensumSubjectDetailActions(
	val onSubjectStatsClick: (subjectCode: String) -> Unit = {},
	val onRelatedSubjectClick: (PensumSubjectDetailNavigationTarget) -> Unit = {},
	val onDismissRequest: () -> Unit = {},
	val onAskAssistant: ((context: AssistantScreenContext) -> Unit)? = null
)
