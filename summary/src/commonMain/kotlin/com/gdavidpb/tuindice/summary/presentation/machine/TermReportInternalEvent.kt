package com.gdavidpb.tuindice.summary.presentation.machine

import com.gdavidpb.tuindice.summary.domain.usecase.error.ObserveTermReportUseCaseError
import com.gdavidpb.tuindice.summary.presentation.model.TermReportItem

/**
 * Internal machine inputs: what the observation of the report says now, the answer to a thumb the service refused, or
 * the end of a thumb's vote. The report arrives already built, since the observation owns the mapping.
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

	// The service refused the thumb [rejected], the vote numbered [thumb]. The screen takes it back only when that vote is
	// still the last one the student sent, and its thumb is still the one on screen.
	class FeedbackRejected(
		val thumb: Int,
		val rejected: Boolean
	) : TermReportInternalEvent

	// The vote numbered [thumb] has ended, accepted or refused. Only the last vote the student sent clears the thumb that
	// waits for it: an earlier vote ending changes nothing.
	class ThumbSettled(
		val thumb: Int
	) : TermReportInternalEvent
}
