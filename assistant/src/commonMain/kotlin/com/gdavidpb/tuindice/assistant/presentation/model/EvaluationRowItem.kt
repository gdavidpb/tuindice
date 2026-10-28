package com.gdavidpb.tuindice.assistant.presentation.model

import com.gdavidpb.tuindice.base.presentation.model.UiText

// One evaluation: its subject, its title, the date it has when it has one, its grade over the maximum, and its state.
// The sheet draws the title and the date apart, as the Evaluations tab does; the chat card joins them in [nameText].
data class EvaluationRowItem(
	val code: String,
	val title: String,
	val dateText: UiText?,
	val nameText: UiText,
	val gradeText: UiText,
	val status: EvaluationStatusItem?
)
