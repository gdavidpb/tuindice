package com.gdavidpb.tuindice.assistant.presentation.model

import com.gdavidpb.tuindice.base.presentation.model.UiText

// One bar of the grade histogram: its grade, how many subjects it holds, the part of the longest bar it fills, and the
// sentence a screen reader says for the row («Nota 4: 16 materias»).
data class HistogramBarItem(
	val grade: String,
	val count: String,
	val fraction: Float,
	val description: UiText
)
