package com.gdavidpb.tuindice.summary.presentation.mapper

import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.summary.domain.usecase.error.ObserveTermReportUseCaseError
import com.gdavidpb.tuindice.summary.presentation.contract.TermReport
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.term_report_failed_message
import tuindice.summary.generated.resources.term_report_failed_record_message
import tuindice.summary.generated.resources.term_report_failed_record_title
import tuindice.summary.generated.resources.term_report_failed_session_message
import tuindice.summary.generated.resources.term_report_failed_title
import tuindice.summary.generated.resources.term_report_no_report_message
import tuindice.summary.generated.resources.term_report_no_report_title

// The state of a report the service does not have, with the words that say so.
internal fun termReportMissingState(): TermReport.State.NoReport {
	return TermReport.State.NoReport(
		title = UiText.Resource(Res.string.term_report_no_report_title),
		message = UiText.Resource(Res.string.term_report_no_report_message)
	)
}

// The state of a report that cannot be read at the moment, with the words of its cause: a record that is not synced yet
// says that the report will come once it is, a refused session says so, and a lost connection offers to try again.
internal fun termReportUnavailableState(cause: ObserveTermReportUseCaseError?): TermReport.State.Failed {
	return when (cause) {
		ObserveTermReportUseCaseError.RecordNotReady -> TermReport.State.Failed(
			title = UiText.Resource(Res.string.term_report_failed_record_title),
			message = UiText.Resource(Res.string.term_report_failed_record_message)
		)

		ObserveTermReportUseCaseError.SessionEnded -> TermReport.State.Failed(
			title = UiText.Resource(Res.string.term_report_failed_title),
			message = UiText.Resource(Res.string.term_report_failed_session_message)
		)

		else -> TermReport.State.Failed(
			title = UiText.Resource(Res.string.term_report_failed_title),
			message = UiText.Resource(Res.string.term_report_failed_message)
		)
	}
}
