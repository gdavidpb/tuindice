package com.gdavidpb.tuindice.record.presentation.model

import androidx.compose.ui.text.AnnotatedString

data class TermItem(
	val termId: String,
	val periodYear: Int,
	val termOrder: Int,
	val shortNameText: String,
	val kind: TermItemKind,
	val gradeText: AnnotatedString,
	val gradeDelta: TermMetricDelta?,
	val gradeSumText: AnnotatedString,
	val gradeSumDelta: TermMetricDelta?,
	val creditsText: AnnotatedString,
	val creditsDelta: TermMetricDelta?,
	val isCurrent: Boolean,
	val canDelete: Boolean,
	val canEdit: Boolean,
	val attempts: List<AttemptItem>
)
