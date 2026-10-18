package com.gdavidpb.tuindice.summary.presentation.machine

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

	data object ReportUnavailable : TermReportInternalEvent

	// The service refused the thumb, so the thumb the student had before is the one that is selected again.
	class FeedbackRejected(
		val previous: Boolean?
	) : TermReportInternalEvent
}
