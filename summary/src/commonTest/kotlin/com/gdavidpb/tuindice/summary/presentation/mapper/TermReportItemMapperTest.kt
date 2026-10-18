package com.gdavidpb.tuindice.summary.presentation.mapper

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.academiccore.domain.model.TermKind
import com.gdavidpb.tuindice.academiccore.domain.model.TermStatistic
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.summary.domain.model.SummaryTermReport
import com.gdavidpb.tuindice.summary.domain.model.SummaryTermReportOutcome
import com.gdavidpb.tuindice.summary.domain.model.SummaryTermReportSubject
import com.gdavidpb.tuindice.summary.presentation.model.TermReportDeltaTone
import com.gdavidpb.tuindice.summary.presentation.model.TermReportPaceItem
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.term_report_delta_same
import tuindice.summary.generated.resources.term_report_delta_up
import tuindice.summary.generated.resources.term_report_legend_approved_many
import tuindice.summary.generated.resources.term_report_pace_empty
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class TermReportItemMapperTest {

	@Test
	fun aPositiveIndexChangeIsWrittenWithItsArrowAndTheTermBeforeIt() {
		val item = reportWith(indexDelta = 1.5).toTermReportItem()

		val delta = assertIs<UiText.Resource>(item.hero.indexDelta?.text)
		assertEquals(Res.string.term_report_delta_up, delta.resource)
		assertEquals("Ene - Mar", delta.args[1])
		assertEquals(TermReportDeltaTone.POSITIVE, item.hero.indexDelta?.tone)
	}

	@Test
	fun aChangeThatRoundsToNothingIsNoChange() {
		val item = reportWith(indexDelta = 0.001).toTermReportItem()

		assertEquals(TermReportDeltaTone.NEUTRAL, item.hero.indexDelta?.tone)
		assertEquals(
			Res.string.term_report_delta_same,
			assertIs<UiText.Resource>(item.hero.indexDelta?.text).resource
		)
	}

	@Test
	fun theLegendCountsEachOutcomeAndTheRowsKeepTheirCodeOrder() {
		val item = reportWith(
			subjects = listOf(
				subject(code = "CI5437", outcome = SummaryTermReportOutcome.APPROVED),
				subject(code = "CI5311", outcome = SummaryTermReportOutcome.APPROVED),
				subject(code = "EG1114", outcome = SummaryTermReportOutcome.FAILED)
			)
		).toTermReportItem()

		assertEquals(
			UiText.Resource(Res.string.term_report_legend_approved_many, listOf(2)),
			item.subjects.legend.first().text
		)
		assertEquals(listOf("CI5437", "CI5311", "EG1114"), item.subjects.rows.map { row -> row.subjectCode })
	}

	@Test
	fun noAverageMeansNoPace_andTheBlockSaysSo() {
		val item = reportWith(averageCredits = null, paceTerms = emptyList()).toTermReportItem()

		val pace = assertIs<TermReportPaceItem.Empty>(item.pace)
		assertEquals(UiText.Resource(Res.string.term_report_pace_empty), pace.text)
	}

	private fun subject(code: String, outcome: SummaryTermReportOutcome) = SummaryTermReportSubject(
		subjectCode = code,
		subjectName = "Materia $code",
		credits = 3,
		outcome = outcome,
		numericGrade = null,
		symbolicGrade = null
	)

	private fun reportWith(
		indexDelta: Double? = null,
		averageCredits: Double? = 4.5,
		paceTerms: List<TermStatistic> = listOf(termStatistic(termKey = "2026-APR_JUL", approvedCredits = 3)),
		subjects: List<SummaryTermReportSubject> = emptyList()
	) = SummaryTermReport(
		insightId = "term-1:2",
		termId = "term-1",
		termKey = "2026-APR_JUL",
		title = "Cerraste con 4,18",
		sections = emptyList(),
		nextActions = emptyList(),
		term = termStatistic(termKey = "2026-APR_JUL", approvedCredits = 3, index = 4.0),
		previousTerm = termStatistic(
			termKey = "2026-JAN_MAR",
			periodCode = AcademicTermPeriod.JAN_MAR,
			termOrder = 20261,
			index = 3.5,
			approvedCredits = 6
		),
		indexDelta = indexDelta,
		cumulativeDelta = null,
		subjects = subjects,
		creditsOfTerm = 3,
		paceTerms = paceTerms,
		averageCredits = averageCredits
	)

	private fun termStatistic(
		termKey: String,
		periodCode: AcademicTermPeriod = AcademicTermPeriod.APR_JUL,
		termOrder: Int = 20263,
		index: Double? = null,
		approvedCredits: Int
	) = TermStatistic(
		termKey = termKey,
		periodCode = periodCode,
		periodYear = 2026,
		termOrder = termOrder,
		kind = TermKind.HISTORICAL,
		index = index,
		cumulativeIndex = index,
		approvedCredits = approvedCredits,
		approvedSubjects = if (approvedCredits > 0) 1 else 0,
		isRegular = periodCode != AcademicTermPeriod.JUL_AUG
	)
}
