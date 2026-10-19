package com.gdavidpb.tuindice.summary.domain.usecase.param

/** The thumbs the student gave a term report: [useful] is the one it chose. */
data class RateTermReportParams(
	val insightId: String,
	val useful: Boolean
)
