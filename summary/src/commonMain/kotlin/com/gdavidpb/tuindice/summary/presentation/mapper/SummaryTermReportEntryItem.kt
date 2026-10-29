package com.gdavidpb.tuindice.summary.presentation.mapper

import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.summary.domain.model.SummaryTermReportEntry
import com.gdavidpb.tuindice.summary.presentation.model.SummaryHubTarget
import com.gdavidpb.tuindice.summary.presentation.model.SummaryTermReportItem
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.term_report_entry_title

// The line of the summary for an unread report: "Tu informe de Abr - Jul 2026", with what the report says under it.
internal fun SummaryTermReportEntry.toSummaryTermReportItem(): SummaryTermReportItem {
	return SummaryTermReportItem(
		insightId = insightId,
		title = UiText.Resource(Res.string.term_report_entry_title, listOf(termNameUnbreakable(periodLabel))),
		subtitle = subtitle,
		target = SummaryHubTarget.TermReport(insightId = insightId)
	)
}
