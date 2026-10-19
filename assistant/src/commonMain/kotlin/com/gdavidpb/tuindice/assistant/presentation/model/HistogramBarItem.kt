package com.gdavidpb.tuindice.assistant.presentation.model

// One bar of the grade histogram: its grade, how many subjects it holds, and the part of the longest bar it fills.
data class HistogramBarItem(
	val grade: String,
	val count: String,
	val fraction: Float
)
