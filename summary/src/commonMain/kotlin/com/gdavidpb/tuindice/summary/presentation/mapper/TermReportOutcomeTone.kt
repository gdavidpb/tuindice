package com.gdavidpb.tuindice.summary.presentation.mapper

import com.gdavidpb.tuindice.summary.domain.model.SummaryTermReportOutcome
import com.gdavidpb.tuindice.summary.presentation.model.TermReportOutcomeTone

// The tone the screen draws an outcome in: the approved with the approval colour, the failed with the error colour, the
// withdrawn with the available colour as the record marks a withdrawal, and the pending in the neutral one.
internal fun SummaryTermReportOutcome.toTone(): TermReportOutcomeTone = when (this) {
	SummaryTermReportOutcome.APPROVED -> TermReportOutcomeTone.APPROVED
	SummaryTermReportOutcome.FAILED -> TermReportOutcomeTone.FAILED
	SummaryTermReportOutcome.WITHDRAWN -> TermReportOutcomeTone.WITHDRAWN
	SummaryTermReportOutcome.PENDING -> TermReportOutcomeTone.PENDING
}
