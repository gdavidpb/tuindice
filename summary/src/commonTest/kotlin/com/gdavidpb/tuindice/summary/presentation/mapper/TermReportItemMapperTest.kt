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
import tuindice.summary.generated.resources.a11y_term_report_delta_same
import tuindice.summary.generated.resources.a11y_term_report_delta_short_up
import tuindice.summary.generated.resources.a11y_term_report_delta_up
import tuindice.summary.generated.resources.term_report_delta_none
import tuindice.summary.generated.resources.term_report_delta_reference
import tuindice.summary.generated.resources.term_report_delta_short_up
import tuindice.summary.generated.resources.term_report_legend_approved_many
import tuindice.summary.generated.resources.term_report_pace_average
import tuindice.summary.generated.resources.term_report_pace_empty
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

// The previous term of the fixtures is January - March 2026.
// As the record names the term before: the screen reader reads it so.
private const val PREVIOUS_TERM = "Ene - Mar 2026"

// As the line under the figure writes it: spaces that do not break and a word joiner on each side of the dash.
private const val PREVIOUS_TERM_UNBREAKABLE = "Ene\u00A0\u2060-\u2060\u00A0Mar\u00A02026"

class TermReportItemMapperTest {

	@Test
	fun aPositiveIndexChangeIsWrittenWithItsArrowAndTheTermBeforeUnderIt() {
		val item = reportWith(indexDelta = 1.5).toTermReportItem()

		val delta = assertIs<UiText.Resource>(item.hero.indexDelta?.text)
		assertEquals(Res.string.term_report_delta_short_up, delta.resource)
		assertEquals(listOf("1.5000"), delta.args)
		val reference = UiText.Resource(Res.string.term_report_delta_reference, listOf(PREVIOUS_TERM_UNBREAKABLE))
		assertEquals(reference, item.hero.indexDelta?.reference)
		assertEquals(TermReportDeltaTone.POSITIVE, item.hero.indexDelta?.tone)
	}

	@Test
	fun aChangeThatRoundsToNothingAtTheFourPrintedDecimalsIsNoChange() {
		val item = reportWith(indexDelta = 0.00001).toTermReportItem()

		assertEquals(TermReportDeltaTone.NEUTRAL, item.hero.indexDelta?.tone)
		assertEquals(UiText.Resource(Res.string.term_report_delta_none), item.hero.indexDelta?.text)
	}

	@Test
	fun aChangeOfOneTenThousandthReadsAsOneTenThousandth_evenWhenTheSubtractionCarriesFloatingError() {
		// 4.1800 - 4.1799 is a little under 0.0001 in binary: the arrow and the figure must still agree.
		val item = reportWith(indexDelta = 4.18 - 4.1799).toTermReportItem()

		val delta = assertIs<UiText.Resource>(item.hero.indexDelta?.text)
		assertEquals(listOf("0.0001"), delta.args)
		assertEquals(TermReportDeltaTone.POSITIVE, item.hero.indexDelta?.tone)
	}

	@Test
	fun aChangeOfATenthReadsWithItsFourDecimals_evenWhenTheSubtractionCarriesFloatingError() {
		// 2.3 - 2.2 is a little under 0.1 in binary: it is still a rise of 0.1000.
		val item = reportWith(indexDelta = 2.3 - 2.2).toTermReportItem()

		val delta = assertIs<UiText.Resource>(item.hero.indexDelta?.text)
		assertEquals(listOf("0.1000"), delta.args)
		assertEquals(TermReportDeltaTone.POSITIVE, item.hero.indexDelta?.tone)
	}

	@Test
	fun aCumulativeChangeOfOneHundredthReadsAsOneHundredthAndItsArrowAgrees() {
		val item = reportWith(cumulativeDelta = 4.18 - 4.17).toTermReportItem()

		val delta = assertIs<UiText.Resource>(item.hero.cumulativeDelta?.text)
		assertEquals(Res.string.term_report_delta_short_up, delta.resource)
		assertEquals(listOf("0.0100"), delta.args)
		assertNull(item.hero.cumulativeDelta?.reference)
	}

	@Test
	fun theChipSaysItsChangeInWordsForTheScreenReader() {
		val item = reportWith(indexDelta = 0.12, cumulativeDelta = 0.12).toTermReportItem()

		val index = assertIs<UiText.Resource>(item.hero.indexDelta?.description)
		assertEquals(Res.string.a11y_term_report_delta_up, index.resource)
		assertEquals(listOf("0.1200", PREVIOUS_TERM), index.args)

		val cumulative = assertIs<UiText.Resource>(item.hero.cumulativeDelta?.description)
		assertEquals(Res.string.a11y_term_report_delta_short_up, cumulative.resource)
		assertEquals(listOf("0.1200"), cumulative.args)
	}

	@Test
	fun aChangeThatIsNoneSaysSoForTheScreenReaderWithTheTermBefore() {
		val item = reportWith(indexDelta = 0.0).toTermReportItem()

		val description = assertIs<UiText.Resource>(item.hero.indexDelta?.description)
		assertEquals(Res.string.a11y_term_report_delta_same, description.resource)
		assertEquals(listOf(PREVIOUS_TERM), description.args)
	}

	@Test
	fun theAverageLineOfTheReportCarriesNoLabel_theLegendWritesTheAverage() {
		val item = reportWith(averageCredits = 13.8).toTermReportItem()

		val pace = assertIs<TermReportPaceItem.Data>(item.pace)
		assertNull(pace.reference.label)
		assertEquals(UiText.Resource(Res.string.term_report_pace_average, listOf("13.8")), pace.averageLegend)
	}

	@Test
	fun theReportCarriesItsShareAndItsConfirmedVote() {
		val item = reportWith().copy(isShareable = true, feedback = false).toTermReportItem()

		assertEquals(true, item.isShareable)
		assertEquals(false, item.confirmedFeedback)
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
		cumulativeDelta: Double? = null,
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
		cumulativeDelta = cumulativeDelta,
		subjects = subjects,
		creditsOfTerm = 3,
		paceTerms = paceTerms,
		averageCredits = averageCredits,
		isShareable = false,
		feedback = null
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
