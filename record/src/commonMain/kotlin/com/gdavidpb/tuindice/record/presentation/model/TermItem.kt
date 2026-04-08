package com.gdavidpb.tuindice.record.presentation.model

import androidx.compose.ui.text.AnnotatedString

data class TermItem(
	val termId: String,
	val shortNameText: String,
	val gradeText: AnnotatedString,
	val gradeDelta: TermMetricDelta?,
	val gradeSumText: AnnotatedString,
	val gradeSumDelta: TermMetricDelta?,
	val creditsText: AnnotatedString,
	val creditsDelta: TermMetricDelta?,
	val isCurrent: Boolean,
	val canDelete: Boolean,
	val attempts: List<AttemptItem>
)

data class TermMetricDelta(
	val text: String,
	val tone: TermMetricDeltaTone
)

enum class TermMetricDeltaTone {
	Positive,
	Negative,
	Neutral,
	Informational
}
