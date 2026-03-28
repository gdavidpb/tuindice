package com.gdavidpb.tuindice.record.presentation.model

import androidx.compose.ui.text.AnnotatedString

data class QuarterItem(
	val quarterId: String,
	val shortNameText: String,
	val gradeText: AnnotatedString,
	val gradeDelta: QuarterMetricDelta?,
	val gradeSumText: AnnotatedString,
	val gradeSumDelta: QuarterMetricDelta?,
	val creditsText: AnnotatedString,
	val creditsDelta: QuarterMetricDelta?,
	val isCurrent: Boolean,
	val canDelete: Boolean,
	val subjects: List<SubjectItem>
)

data class QuarterMetricDelta(
	val text: String,
	val tone: QuarterMetricDeltaTone
)

enum class QuarterMetricDeltaTone {
	Positive,
	Negative,
	Neutral,
	Informational
}
