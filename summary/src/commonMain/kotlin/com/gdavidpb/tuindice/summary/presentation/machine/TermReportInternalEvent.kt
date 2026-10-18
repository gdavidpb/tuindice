package com.gdavidpb.tuindice.summary.presentation.machine

import com.gdavidpb.tuindice.summary.domain.usecase.error.ObserveTermReportUseCaseError
import com.gdavidpb.tuindice.summary.presentation.model.TermReportItem

/**
 * Internal machine inputs: what the observation of the report says now, or the answer to a thumb the service refused.
 * The report arrives already built, since the observation owns the mapping.
 */
sealed interface TermReportInternalEvent {
	class ReportObserved(
		val report: TermReportItem
	) : TermReportInternalEvent

	data object ReportMissing : TermReportInternalEvent

	// The report cannot be read at the moment, for the cause [cause] names; null when the cause is not one the use case
	// knows.
	class ReportUnavailable(
		val cause: ObserveTermReportUseCaseError?
	) : TermReportInternalEvent

	// The service refused the thumb [rejected], which was the one on screen: the screen takes it back, unless it has
	// moved on to another thumb since.
	class FeedbackRejected(
		val rejected: Boolean
	) : TermReportInternalEvent
}
