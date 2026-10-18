package com.gdavidpb.tuindice.summary.domain.usecase.param

/**
 * The report to read: its [insightId], and whether the service answered the request to read the reports again, which
 * tells a report that is not there yet from one that is not there at all.
 */
data class ObserveTermReportParams(
	val insightId: String,
	val isRefreshed: Boolean
)
