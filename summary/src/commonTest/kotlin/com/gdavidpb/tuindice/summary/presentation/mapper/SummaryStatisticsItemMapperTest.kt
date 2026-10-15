package com.gdavidpb.tuindice.summary.presentation.mapper

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.academiccore.domain.model.CreditOutcomes
import com.gdavidpb.tuindice.academiccore.domain.model.GradeHistogram
import com.gdavidpb.tuindice.academiccore.domain.model.SubjectOutcomes
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.summary.domain.model.SummaryCreditsProgress
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsCreditsItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsCurrentTermItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsFactItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsGradesItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsIndexItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsOutcome
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsPaceItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsSubjectsItem
import com.gdavidpb.tuindice.summary.testing.statisticsHubOf
import com.gdavidpb.tuindice.summary.testing.statisticsOf
import com.gdavidpb.tuindice.summary.testing.statisticsTerm
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.summary_career_percent
import tuindice.summary.generated.resources.summary_percent_format
import tuindice.summary.generated.resources.summary_statistics_credits_approved_many
import tuindice.summary.generated.resources.summary_statistics_credits_empty
import tuindice.summary.generated.resources.summary_statistics_credits_figure_many
import tuindice.summary.generated.resources.summary_statistics_credits_pensum
import tuindice.summary.generated.resources.summary_statistics_credits_share_approved_many
import tuindice.summary.generated.resources.summary_statistics_current_title
import tuindice.summary.generated.resources.summary_statistics_grades_empty
import tuindice.summary.generated.resources.summary_statistics_grades_info
import tuindice.summary.generated.resources.summary_statistics_grades_share_one
import tuindice.summary.generated.resources.summary_statistics_grades_tooltip_many
import tuindice.summary.generated.resources.summary_statistics_grades_unreported
import tuindice.summary.generated.resources.summary_statistics_index_best
import tuindice.summary.generated.resources.summary_statistics_index_caption
import tuindice.summary.generated.resources.summary_statistics_index_empty
import tuindice.summary.generated.resources.summary_statistics_index_suffix
import tuindice.summary.generated.resources.summary_statistics_index_worst
import tuindice.summary.generated.resources.summary_statistics_pace_empty
import tuindice.summary.generated.resources.summary_statistics_subjects_empty
import tuindice.summary.generated.resources.summary_statistics_subjects_figure_many
import tuindice.summary.generated.resources.summary_statistics_subjects_first_try
import tuindice.summary.generated.resources.summary_statistics_subjects_no_result
import tuindice.summary.generated.resources.summary_statistics_subjects_repeated_many
import tuindice.summary.generated.resources.summary_statistics_subjects_share_approved_many
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

// The blocks of the Statistics screen that count the closed terms: the index, Materias, Créditos and Tus notas. The
// blocks of the pace and of the term in progress have their own tests.
class SummaryStatisticsItemMapperTest {
	/* The index */

	@Test
	fun when_theTermsHaveIndexes_then_theIndexDrawsThemAndNamesTheBestAndTheWorst() {
		val statistics = statisticsOf(
			terms = listOf(
				statisticsTerm(year = 2023, index = 4.5),
				statisticsTerm(year = 2024, periodCode = AcademicTermPeriod.JAN_MAR, index = 3.5).copy(cumulativeIndex = 4.0)
			)
		)

		val index = statisticsHubOf(statistics = statistics).toSummaryStatisticsItem()!!.index

		val data = assertIs<SummaryStatisticsIndexItem.Content.Data>(index.content)
		assertEquals(2, data.points.size)
		assertEquals(4.0f, data.grade)
		assertEquals(UiText.Resource(Res.string.summary_statistics_index_suffix), data.label)
		assertEquals(
			UiText.Resource(Res.string.summary_statistics_index_caption, listOf("Ene - Mar 2024")),
			data.caption
		)
		assertEquals(
			UiText.Resource(Res.string.summary_statistics_index_best, listOf("Sep - Dic 2023", "4.5000")),
			data.best
		)
		assertEquals(
			UiText.Resource(Res.string.summary_statistics_index_worst, listOf("Ene - Mar 2024", "3.5000")),
			data.worst
		)
	}

	@Test
	fun when_oneRegularTermIsRanked_then_onlyTheBestIsNamed() {
		val statistics = statisticsOf(terms = listOf(statisticsTerm(year = 2024, index = 4.0)))

		val index = statisticsHubOf(statistics = statistics).toSummaryStatisticsItem()!!.index

		val data = assertIs<SummaryStatisticsIndexItem.Content.Data>(index.content)
		assertTrue(data.best != null)
		assertNull(data.worst)
	}

	@Test
	fun when_noTermHasAnIndex_then_theIndexSaysItWillAppear() {
		val statistics = statisticsOf(terms = listOf(statisticsTerm(year = 2024, index = null).copy(cumulativeIndex = null)))

		val index = statisticsHubOf(statistics = statistics).toSummaryStatisticsItem()!!.index

		assertEquals(
			SummaryStatisticsIndexItem.Content.Empty(text = UiText.Resource(Res.string.summary_statistics_index_empty)),
			index.content
		)
	}

	/* The whole screen when there is nothing closed yet */

	@Test
	fun when_theRecordHasNoClosedTerm_then_everyBlockIsThereWithItsText() {
		val item = statisticsHubOf(statistics = statisticsOf()).toSummaryStatisticsItem()!!

		assertEquals(UiText.Resource(Res.string.summary_statistics_current_title), item.currentTerm.title)
		assertIs<SummaryStatisticsCurrentTermItem.Content.NoTerm>(item.currentTerm.content)
		assertEquals(
			SummaryStatisticsIndexItem.Content.Empty(UiText.Resource(Res.string.summary_statistics_index_empty)),
			item.index.content
		)
		assertEquals(
			SummaryStatisticsSubjectsItem.Content.Empty(UiText.Resource(Res.string.summary_statistics_subjects_empty)),
			item.subjects.content
		)
		assertEquals(
			SummaryStatisticsCreditsItem.Content.Empty(UiText.Resource(Res.string.summary_statistics_credits_empty)),
			item.credits.content
		)
		assertEquals(
			SummaryStatisticsGradesItem.Content.Empty(UiText.Resource(Res.string.summary_statistics_grades_empty)),
			item.grades.content
		)
		assertEquals(
			SummaryStatisticsPaceItem.Content.Empty(UiText.Resource(Res.string.summary_statistics_pace_empty)),
			item.pace.content
		)
	}

	@Test
	fun when_theHubHasNoStatistics_then_thereIsNoScreenToShow() {
		assertNull(statisticsHubOf(statistics = null).toSummaryStatisticsItem())
	}

	/* Materias and Créditos */

	@Test
	fun when_subjectsHaveResults_then_theFigureIsTheSumOfTheOutcomes_andTheFactsCountTheFirstTries() {
		val statistics = statisticsOf(
			subjects = SubjectOutcomes(
				approved = 44,
				failed = 2,
				withdrawn = 1,
				noResult = 0,
				approvedFirstTry = 44,
				firstTryBase = 46,
				repeated = 0
			)
		)

		val subjects = statisticsHubOf(statistics = statistics).toSummaryStatisticsItem()!!.subjects

		val data = assertIs<SummaryStatisticsSubjectsItem.Content.Data>(subjects.content)
		assertEquals("47", data.figure.value)
		assertEquals(UiText.Resource(Res.string.summary_statistics_subjects_figure_many), data.figure.label)
		assertEquals(
			listOf(
				SummaryStatisticsOutcome.APPROVED,
				SummaryStatisticsOutcome.FAILED,
				SummaryStatisticsOutcome.WITHDRAWN
			),
			data.segments.map { segment -> segment.outcome }
		)
		assertEquals(
			UiText.Resource(Res.string.summary_statistics_subjects_share_approved_many, listOf(44, percent(94))),
			data.segments.first().tooltip.title
		)
		assertEquals(
			listOf(
				SummaryStatisticsFactItem(
					value = "44 / 46",
					label = UiText.Resource(Res.string.summary_statistics_subjects_first_try)
				),
				SummaryStatisticsFactItem(
					value = "0",
					label = UiText.Resource(Res.string.summary_statistics_subjects_repeated_many)
				)
			),
			data.facts
		)
	}

	@Test
	fun when_noSubjectHasAResult_then_theFirstTryFactLeavesOut() {
		val statistics = statisticsOf(
			subjects = SubjectOutcomes(
				approved = 0,
				failed = 0,
				withdrawn = 2,
				noResult = 0,
				approvedFirstTry = 0,
				firstTryBase = 0,
				repeated = 0
			)
		)

		val subjects = statisticsHubOf(statistics = statistics).toSummaryStatisticsItem()!!.subjects

		val data = assertIs<SummaryStatisticsSubjectsItem.Content.Data>(subjects.content)
		assertEquals(
			listOf(
				SummaryStatisticsFactItem(
					value = "0",
					label = UiText.Resource(Res.string.summary_statistics_subjects_repeated_many)
				)
			),
			data.facts
		)
	}

	@Test
	fun when_someSubjectsHaveNoResult_then_theyAreACompactFigureOutOfTheBar() {
		val statistics = statisticsOf(
			subjects = SubjectOutcomes(
				approved = 44,
				failed = 2,
				withdrawn = 1,
				noResult = 2,
				approvedFirstTry = 44,
				firstTryBase = 46,
				repeated = 0
			)
		)

		val subjects = statisticsHubOf(statistics = statistics).toSummaryStatisticsItem()!!.subjects

		val data = assertIs<SummaryStatisticsSubjectsItem.Content.Data>(subjects.content)
		assertEquals("49", data.figure.value)
		assertEquals(
			listOf(
				SummaryStatisticsOutcome.APPROVED,
				SummaryStatisticsOutcome.FAILED,
				SummaryStatisticsOutcome.WITHDRAWN
			),
			data.segments.map { segment -> segment.outcome }
		)
		// The shares are out of the bar, which holds the subjects with a result: 44 of 47.
		assertEquals(
			UiText.Resource(Res.string.summary_statistics_subjects_share_approved_many, listOf(44, percent(94))),
			data.segments.first().tooltip.title
		)
		assertEquals(
			SummaryStatisticsFactItem(
				value = "2",
				label = UiText.Resource(Res.string.summary_statistics_subjects_no_result)
			),
			data.facts.last()
		)
	}

	@Test
	fun when_oneSubjectHasNoResult_then_itsFactCountsOne() {
		val statistics = statisticsOf(
			subjects = SubjectOutcomes(
				approved = 44,
				failed = 2,
				withdrawn = 1,
				noResult = 1,
				approvedFirstTry = 44,
				firstTryBase = 46,
				repeated = 0
			)
		)

		val subjects = statisticsHubOf(statistics = statistics).toSummaryStatisticsItem()!!.subjects

		val data = assertIs<SummaryStatisticsSubjectsItem.Content.Data>(subjects.content)
		assertEquals(
			SummaryStatisticsFactItem(
				value = "1",
				label = UiText.Resource(Res.string.summary_statistics_subjects_no_result)
			),
			data.facts.last()
		)
	}

	@Test
	fun when_noSubjectHasNoResult_then_thereIsNoFactForIt() {
		val statistics = statisticsOf(
			subjects = SubjectOutcomes(
				approved = 44,
				failed = 2,
				withdrawn = 1,
				noResult = 0,
				approvedFirstTry = 44,
				firstTryBase = 46,
				repeated = 0
			)
		)

		val subjects = statisticsHubOf(statistics = statistics).toSummaryStatisticsItem()!!.subjects

		val data = assertIs<SummaryStatisticsSubjectsItem.Content.Data>(subjects.content)
		assertTrue(
			data.facts.none { fact ->
				fact.label == UiText.Resource(Res.string.summary_statistics_subjects_no_result)
			}
		)
	}

	@Test
	fun when_theRecordHasCreditsAndAPensum_then_theBarIsFollowedByThePensumShare() {
		val statistics = statisticsOf(
			credits = CreditOutcomes(approved = 152, failed = 8, withdrawn = 4)
		).copy(
			pensumProgress = SummaryCreditsProgress(approvedCredits = 152, totalCredits = 205)
		)

		val credits = statisticsHubOf(statistics = statistics).toSummaryStatisticsItem()!!.credits

		val data = assertIs<SummaryStatisticsCreditsItem.Content.Data>(credits.content)
		assertEquals("164", data.figure.value)
		assertEquals(UiText.Resource(Res.string.summary_statistics_credits_figure_many), data.figure.label)
		assertEquals(
			UiText.Resource(Res.string.summary_statistics_credits_share_approved_many, listOf(152, percent(93))),
			data.segments.first().tooltip.title
		)
		assertEquals(
			UiText.Resource(Res.string.summary_statistics_credits_approved_many, listOf(152)),
			data.segments.first().legend
		)
		val pensum = data.pensum!!
		assertEquals("152", pensum.figure.value)
		assertEquals(UiText.Resource(Res.string.summary_statistics_credits_pensum, listOf(205)), pensum.figure.label)
		assertEquals(UiText.Resource(Res.string.summary_career_percent, listOf(74)), pensum.figure.trailing)
	}

	@Test
	fun when_noPensumIsCached_then_theCreditsLeaveThePensumOut() {
		val statistics = statisticsOf(credits = CreditOutcomes(approved = 15, failed = 0, withdrawn = 0))

		val credits = statisticsHubOf(statistics = statistics).toSummaryStatisticsItem()!!.credits

		val data = assertIs<SummaryStatisticsCreditsItem.Content.Data>(credits.content)
		assertNull(data.pensum)
	}

	/* Tus notas */

	@Test
	fun when_gradesAreCounted_then_theUnreportedAreAFigureAndTheInfoExplainsTheBars() {
		val histogram = GradeHistogram(
			countsByGrade = mapOf(1 to 0, 2 to 2, 3 to 7, 4 to 21, 5 to 16),
			unreported = 3
		)

		val grades = statisticsHubOf(statistics = statisticsOf(grades = histogram))
			.toSummaryStatisticsItem()!!
			.grades

		val data = assertIs<SummaryStatisticsGradesItem.Content.Data>(grades.content)
		assertEquals(5, data.bars.size)
		assertEquals(
			listOf(
				SummaryStatisticsFactItem(
					value = "3",
					label = UiText.Resource(Res.string.summary_statistics_grades_unreported)
				)
			),
			data.facts
		)
		assertEquals(UiText.Resource(Res.string.summary_statistics_grades_info), grades.info)
		assertEquals(
			UiText.Resource(Res.string.summary_statistics_grades_tooltip_many, listOf(4, 21)),
			data.bars[3].tooltip.title
		)
	}

	@Test
	fun when_noAttemptIsUnreported_then_noFactIsWrittenAndNoMostFrequentGradeIsNamed() {
		val histogram = GradeHistogram(countsByGrade = mapOf(1 to 0, 2 to 0, 3 to 0, 4 to 5, 5 to 5), unreported = 0)

		val grades = statisticsHubOf(statistics = statisticsOf(grades = histogram))
			.toSummaryStatisticsItem()!!
			.grades

		val data = assertIs<SummaryStatisticsGradesItem.Content.Data>(grades.content)
		assertEquals(emptyList<SummaryStatisticsFactItem>(), data.facts)
		assertEquals(UiText.Resource(Res.string.summary_statistics_grades_info), grades.info)
	}

	@Test
	fun when_theOnlyGradeIsOneAttempt_then_itsShareIsOfTheSingleGrade() {
		val histogram = GradeHistogram(countsByGrade = mapOf(1 to 0, 2 to 0, 3 to 0, 4 to 1, 5 to 0), unreported = 0)

		val data = statisticsHubOf(statistics = statisticsOf(grades = histogram))
			.toSummaryStatisticsItem()!!
			.grades
			.content as SummaryStatisticsGradesItem.Content.Data

		assertEquals(
			listOf(UiText.Resource(Res.string.summary_statistics_grades_share_one, listOf(percent(100)))),
			data.bars[3].tooltip.lines
		)
	}

	@Test
	fun when_gradesAreCounted_then_eachBarWritesItsCountAboveIt() {
		val histogram = GradeHistogram(
			countsByGrade = mapOf(1 to 0, 2 to 2, 3 to 7, 4 to 21, 5 to 16),
			unreported = 3
		)

		val grades = statisticsHubOf(statistics = statisticsOf(grades = histogram))
			.toSummaryStatisticsItem()!!
			.grades

		val data = assertIs<SummaryStatisticsGradesItem.Content.Data>(grades.content)
		assertEquals(
			listOf(UiText.Raw("0"), UiText.Raw("2"), UiText.Raw("7"), UiText.Raw("21"), UiText.Raw("16")),
			data.bars.map { bar -> bar.valueLabel }
		)
	}
}

private fun percent(value: Int): UiText = UiText.Resource(Res.string.summary_percent_format, listOf(value, "%"))
