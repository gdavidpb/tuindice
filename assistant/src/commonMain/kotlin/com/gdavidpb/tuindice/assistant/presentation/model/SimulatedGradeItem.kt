package com.gdavidpb.tuindice.assistant.presentation.model

import com.gdavidpb.tuindice.base.presentation.model.UiText

// One simulated grade: its subject, its result, and the grade when it has one.
data class SimulatedGradeItem(
	val code: String,
	val outcomeText: UiText,
	val gradeText: String?
)
