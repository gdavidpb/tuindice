package com.gdavidpb.tuindice.summary.domain.usecase.param

import com.gdavidpb.tuindice.summary.domain.usecase.error.UpdateTermReportsUseCaseError

/**
 * The report to read: its [insightId], and whether the service answered the request to read the reports again, which
 * tells a report that is not there yet from one that is not there at all. When it did not answer, [refreshError] says
 * why: a session the service refused is told apart from a connection that failed.
 */
data class ObserveTermReportParams(
	val insightId: String,
	val isRefreshed: Boolean,
	val refreshError: UpdateTermReportsUseCaseError? = null
)
