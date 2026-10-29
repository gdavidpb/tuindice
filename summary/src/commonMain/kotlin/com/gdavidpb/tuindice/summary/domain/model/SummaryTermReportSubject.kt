package com.gdavidpb.tuindice.summary.domain.model

/**
 * A subject of the term: its [subjectCode] and [subjectName], its [credits], what the term says of it ([outcome]) and
 * the grade it carries, numeric or symbolic, when it has one.
 */
data class SummaryTermReportSubject(
	val subjectCode: String,
	val subjectName: String,
	val credits: Int,
	val outcome: SummaryTermReportOutcome,
	val numericGrade: Int?,
	val symbolicGrade: String?
)
