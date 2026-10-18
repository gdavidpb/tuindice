package com.gdavidpb.tuindice.summary.presentation.mapper

import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.summary.domain.model.SummaryTermReportEntry
import com.gdavidpb.tuindice.summary.presentation.model.SummaryHubTarget
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.term_report_entry_title
import kotlin.test.Test
import kotlin.test.assertEquals

class SummaryTermReportEntryItemTest {
	@Test
	fun theLineNamesItsTermWithNoBreakInsideTheName() {
		val item = SummaryTermReportEntry(
			insightId = "term-1:2",
			termKey = "2026-JAN_MAR",
			periodLabel = "Ene - Mar 2026",
			subtitle = "Cerraste con 4,18"
		).toSummaryTermReportItem()

		assertEquals(
			UiText.Resource(Res.string.term_report_entry_title, listOf("Ene - Mar 2026")),
			item.title
		)
		assertEquals(SummaryHubTarget.TermReport(insightId = "term-1:2"), item.target)
	}
}
