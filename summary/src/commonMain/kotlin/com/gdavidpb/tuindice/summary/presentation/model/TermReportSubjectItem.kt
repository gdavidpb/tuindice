package com.gdavidpb.tuindice.summary.presentation.model

import androidx.compose.runtime.Immutable

/**
 * One subject of the term: its code and name, its grade as the badge writes it (null when it has none) and the tone of
 * its outcome.
 */
@Immutable
data class TermReportSubjectItem(
	val subjectCode: String,
	val subjectName: String,
	val grade: String?,
	val tone: TermReportOutcomeTone
)
