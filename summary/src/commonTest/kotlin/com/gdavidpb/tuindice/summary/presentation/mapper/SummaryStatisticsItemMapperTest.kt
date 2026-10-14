package com.gdavidpb.tuindice.summary.presentation.mapper

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.academiccore.domain.model.CreditOutcomes
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationType
import com.gdavidpb.tuindice.academiccore.domain.model.GradeHistogram
import com.gdavidpb.tuindice.academiccore.domain.model.PaceStatistics
import com.gdavidpb.tuindice.academiccore.domain.model.SubjectOutcomes
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.persistence.domain.model.PensumCreditsProgress
import com.gdavidpb.tuindice.summary.presentation.model.SummaryHubTarget
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsCreditsItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsCurrentTermItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsGradesItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsIndexItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsOutcome
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsPaceItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsSubjectsItem
import com.gdavidpb.tuindice.summary.testing.currentTermOf
import com.gdavidpb.tuindice.summary.testing.evaluationTypeOf
import com.gdavidpb.tuindice.summary.testing.progressTermOf
import com.gdavidpb.tuindice.summary.testing.statisticsHubOf
import com.gdavidpb.tuindice.summary.testing.statisticsOf
import com.gdavidpb.tuindice.summary.testing.statisticsTerm
import com.gdavidpb.tuindice.summary.testing.ungradedAttentionOf
import kotlinx.datetime.LocalDate
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.summary_career_percent
import tuindice.summary.generated.resources.summary_points_none
import tuindice.summary.generated.resources.summary_statistics_credits_approved_many
import tuindice.summary.generated.resources.summary_statistics_credits_empty
import tuindice.summary.generated.resources.summary_statistics_credits_figure_many
import tuindice.summary.generated.resources.summary_statistics_credits_pensum
import tuindice.summary.generated.resources.summary_statistics_credits_share_approved_many
import tuindice.summary.generated.resources.summary_statistics_current_figure
import tuindice.summary.generated.resources.summary_statistics_current_graded_one
import tuindice.summary.generated.resources.summary_statistics_current_overdue_many
import tuindice.summary.generated.resources.summary_statistics_current_score
import tuindice.summary.generated.resources.summary_statistics_current_title
import tuindice.summary.generated.resources.summary_statistics_grades_empty
import tuindice.summary.generated.resources.summary_statistics_grades_mode
import tuindice.summary.generated.resources.summary_statistics_grades_note
import tuindice.summary.generated.resources.summary_statistics_grades_share_one
import tuindice.summary.generated.resources.summary_statistics_grades_tooltip_many
import tuindice.summary.generated.resources.summary_statistics_grades_unreported_many
import tuindice.summary.generated.resources.summary_statistics_index_best
import tuindice.summary.generated.resources.summary_statistics_index_caption
import tuindice.summary.generated.resources.summary_statistics_index_empty
import tuindice.summary.generated.resources.summary_statistics_index_worst
import tuindice.summary.generated.resources.summary_statistics_pace_complete
import tuindice.summary.generated.resources.summary_statistics_pace_empty
import tuindice.summary.generated.resources.summary_statistics_pace_figure
import tuindice.summary.generated.resources.summary_statistics_pace_invite_message
import tuindice.summary.generated.resources.summary_statistics_pace_invite_title
import tuindice.summary.generated.resources.summary_statistics_pace_projection_many
import tuindice.summary.generated.resources.summary_statistics_pace_projection_one
import tuindice.summary.generated.resources.summary_statistics_pace_projection_pending
import tuindice.summary.generated.resources.summary_statistics_subjects_empty
import tuindice.summary.generated.resources.summary_statistics_subjects_figure_many
import tuindice.summary.generated.resources.summary_statistics_subjects_first_try_many
import tuindice.summary.generated.resources.summary_statistics_subjects_no_repeated
import tuindice.summary.generated.resources.summary_statistics_subjects_share_approved_many
import tuindice.summary.generated.resources.summary_statistics_type_quiz
import tuindice.summary.generated.resources.summary_statistics_type_tooltip_quiz
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

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
		assertEquals("4.0000", data.figure.value)
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
	fun when_subjectsHaveResults_then_theFigureIsTheSumOfTheOutcomes_andTheNotesCountTheFirstTries() {
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
			UiText.Resource(Res.string.summary_statistics_subjects_share_approved_many, listOf(44, "94 %")),
			data.segments.first().tooltip.title
		)
		assertEquals(
			listOf(
				UiText.Resource(Res.string.summary_statistics_subjects_first_try_many, listOf(44, 46)),
				UiText.Resource(Res.string.summary_statistics_subjects_no_repeated)
			),
			data.notes
		)
	}

	@Test
	fun when_noSubjectHasAResult_then_theNotesLeaveTheFirstTryOut() {
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
		assertEquals(listOf(UiText.Resource(Res.string.summary_statistics_subjects_no_repeated)), data.notes)
	}

	@Test
	fun when_theRecordHasCreditsAndAPensum_then_theBarIsFollowedByThePensumShare() {
		val statistics = statisticsOf(
			credits = CreditOutcomes(approved = 152, failed = 8, withdrawn = 4)
		).copy(
			pensumProgress = PensumCreditsProgress(approvedCredits = 152, totalCredits = 205)
		)

		val credits = statisticsHubOf(statistics = statistics).toSummaryStatisticsItem()!!.credits

		val data = assertIs<SummaryStatisticsCreditsItem.Content.Data>(credits.content)
		assertEquals("164", data.figure.value)
		assertEquals(UiText.Resource(Res.string.summary_statistics_credits_figure_many), data.figure.label)
		assertEquals(
			UiText.Resource(Res.string.summary_statistics_credits_share_approved_many, listOf(152, "93 %")),
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
	fun when_gradesAreCounted_then_theMostFrequentIsNamed_andTheUnreportedAreCounted() {
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
				UiText.Resource(Res.string.summary_statistics_grades_note),
				UiText.Resource(Res.string.summary_statistics_grades_mode, listOf(4))
			),
			data.notes
		)
		assertEquals(UiText.Resource(Res.string.summary_statistics_grades_unreported_many, listOf(3)), data.unreported)
		assertEquals(
			UiText.Resource(Res.string.summary_statistics_grades_tooltip_many, listOf(4, 21)),
			data.bars[3].tooltip.title
		)
	}

	@Test
	fun when_twoGradesTieForTheMostFrequent_then_theHighestOfThemIsNamed() {
		val histogram = GradeHistogram(countsByGrade = mapOf(1 to 0, 2 to 0, 3 to 0, 4 to 5, 5 to 5), unreported = 0)

		val data = statisticsHubOf(statistics = statisticsOf(grades = histogram))
			.toSummaryStatisticsItem()!!
			.grades
			.content as SummaryStatisticsGradesItem.Content.Data

		assertEquals(UiText.Resource(Res.string.summary_statistics_grades_mode, listOf(5)), data.notes.last())
	}

	@Test
	fun when_theOnlyGradeIsOneAttempt_then_itsShareIsOfTheSingleGrade() {
		val histogram = GradeHistogram(countsByGrade = mapOf(1 to 0, 2 to 0, 3 to 0, 4 to 1, 5 to 0), unreported = 0)

		val data = statisticsHubOf(statistics = statisticsOf(grades = histogram))
			.toSummaryStatisticsItem()!!
			.grades
			.content as SummaryStatisticsGradesItem.Content.Data

		assertEquals(
			listOf(UiText.Resource(Res.string.summary_statistics_grades_share_one, listOf("100 %"))),
			data.bars[3].tooltip.lines
		)
	}

	/* Ritmo */

	@Test
	fun when_aJulyAugustTermCloses_then_itIsDrawnButLeavesTheAverageOut() {
		val statistics = statisticsOf(
			terms = listOf(
				statisticsTerm(year = 2024, approvedCredits = 15),
				statisticsTerm(year = 2024, periodCode = AcademicTermPeriod.JUL_AUG, approvedCredits = 6)
			)
		).copy(
			pace = PaceStatistics(averageCredits = 15.0, remainingTerms = null)
		)

		val pace = statisticsHubOf(statistics = statistics).toSummaryStatisticsItem()!!.pace

		val data = assertIs<SummaryStatisticsPaceItem.Content.Data>(pace.content)
		assertEquals(2, data.bars.size)
		assertEquals(6f, data.bars[1].value)
		assertEquals("15.0", data.figure.value)
		assertEquals(UiText.Resource(Res.string.summary_statistics_pace_figure), data.figure.label)
	}

	@Test
	fun when_noRegularTermCloses_then_thePaceSaysItWillAppear() {
		val statistics = statisticsOf(
			terms = listOf(statisticsTerm(year = 2024, periodCode = AcademicTermPeriod.JUL_AUG, approvedCredits = 6))
		)

		val pace = statisticsHubOf(statistics = statistics).toSummaryStatisticsItem()!!.pace

		assertEquals(
			SummaryStatisticsPaceItem.Content.Empty(UiText.Resource(Res.string.summary_statistics_pace_empty)),
			pace.content
		)
	}

	@Test
	fun when_noPensumIsCached_then_thePaceInvitesToOpenIt() {
		val statistics = statisticsOf(
			terms = listOf(statisticsTerm(year = 2024))
		).copy(
			pace = PaceStatistics(averageCredits = 15.0, remainingTerms = null)
		)

		val data = statisticsHubOf(statistics = statistics)
			.toSummaryStatisticsItem()!!
			.pace
			.content as SummaryStatisticsPaceItem.Content.Data

		assertEquals(
			SummaryStatisticsPaceItem.Projection.NoPensum(
				title = UiText.Resource(Res.string.summary_statistics_pace_invite_title),
				message = UiText.Resource(Res.string.summary_statistics_pace_invite_message)
			),
			data.projection
		)
	}

	@Test
	fun when_oneRegularTermCloses_then_itsBarShowsAndTheProjectionWaits() {
		val statistics = statisticsOf(
			terms = listOf(statisticsTerm(year = 2024, approvedCredits = 15))
		).copy(
			pace = PaceStatistics(averageCredits = 15.0, remainingTerms = null),
			pensumProgress = PensumCreditsProgress(approvedCredits = 15, totalCredits = 205)
		)

		val data = statisticsHubOf(statistics = statistics)
			.toSummaryStatisticsItem()!!
			.pace
			.content as SummaryStatisticsPaceItem.Content.Data

		assertEquals(1, data.bars.size)
		assertEquals(15f, data.reference.value)
		assertEquals(
			SummaryStatisticsPaceItem.Projection.Pending(
				UiText.Resource(Res.string.summary_statistics_pace_projection_pending)
			),
			data.projection
		)
	}

	@Test
	fun when_theRemainingTermsAreKnown_then_theProjectionCountsThemAndSaysSo() {
		val statistics = statisticsOf(
			terms = listOf(
				statisticsTerm(year = 2024, approvedCredits = 15),
				statisticsTerm(year = 2025, approvedCredits = 15)
			)
		).copy(
			pace = PaceStatistics(averageCredits = 15.0, remainingTerms = 4),
			pensumProgress = PensumCreditsProgress(approvedCredits = 30, totalCredits = 205)
		)

		val pace = statisticsHubOf(statistics = statistics).toSummaryStatisticsItem()!!.pace
		val data = pace.content as SummaryStatisticsPaceItem.Content.Data
		val projection = data.projection as SummaryStatisticsPaceItem.Projection.Remaining

		assertEquals(
			UiText.Resource(Res.string.summary_statistics_pace_projection_many, listOf(4, 205)),
			projection.text
		)
	}

	@Test
	fun when_oneTermIsLeftToProject_then_theProjectionSaysOneTerm() {
		val statistics = statisticsOf(
			terms = listOf(
				statisticsTerm(year = 2024, approvedCredits = 15),
				statisticsTerm(year = 2025, approvedCredits = 15)
			)
		).copy(
			pace = PaceStatistics(averageCredits = 15.0, remainingTerms = 1),
			pensumProgress = PensumCreditsProgress(approvedCredits = 190, totalCredits = 205)
		)

		val pace = statisticsHubOf(statistics = statistics).toSummaryStatisticsItem()!!.pace
		val data = pace.content as SummaryStatisticsPaceItem.Content.Data
		val projection = data.projection as SummaryStatisticsPaceItem.Projection.Remaining

		assertEquals(
			UiText.Resource(Res.string.summary_statistics_pace_projection_one, listOf(1, 205)),
			projection.text
		)
	}

	@Test
	fun when_thePensumIsComplete_then_thePaceSaysSo() {
		val statistics = statisticsOf(
			terms = listOf(
				statisticsTerm(year = 2024, approvedCredits = 15),
				statisticsTerm(year = 2025, approvedCredits = 15)
			)
		).copy(
			pace = PaceStatistics(averageCredits = 15.0, remainingTerms = null),
			pensumProgress = PensumCreditsProgress(approvedCredits = 205, totalCredits = 205)
		)

		val data = statisticsHubOf(statistics = statistics)
			.toSummaryStatisticsItem()!!
			.pace
			.content as SummaryStatisticsPaceItem.Content.Data

		assertEquals(
			SummaryStatisticsPaceItem.Projection.Complete(UiText.Resource(Res.string.summary_statistics_pace_complete)),
			data.projection
		)
	}

	@Test
	fun when_theAxisNamesCollide_then_theyKeepTheirThreeLetterMonths() {
		val statistics = statisticsOf(
			terms = listOf(
				statisticsTerm(year = 2024, periodCode = AcademicTermPeriod.JAN_MAR),
				statisticsTerm(year = 2024, periodCode = AcademicTermPeriod.JAN_MAY),
				statisticsTerm(year = 2023, periodCode = AcademicTermPeriod.SEP_DEC),
				statisticsTerm(year = 2023, periodCode = AcademicTermPeriod.JUL_AUG)
			)
		).copy(
			pace = PaceStatistics(averageCredits = 15.0, remainingTerms = null)
		)

		val pace = statisticsHubOf(statistics = statistics).toSummaryStatisticsItem()!!.pace
		val bars = (pace.content as SummaryStatisticsPaceItem.Content.Data).bars

		assertEquals(
			listOf(UiText.Raw("Ene-Mar 24"), UiText.Raw("Ene-May 24"), UiText.Raw("S-D 23"), UiText.Raw("J-A 23")),
			bars.map { bar -> bar.axisLabel }
		)
	}

	/* Este trimestre */

	@Test
	fun when_theTermIsAbsent_then_thisTermInvitesToEnrollAndHasNoLink() {
		val current = statisticsHubOf(statistics = statisticsOf()).toSummaryStatisticsItem()!!.currentTerm

		assertEquals(UiText.Resource(Res.string.summary_statistics_current_title), current.title)
		assertNull(current.termLink)
		assertNull(current.termTarget)
	}

	@Test
	fun when_theEvaluationsAreNotDownloaded_then_thisTermInvitesToOpenThemThroughItsAction() {
		val statistics = statisticsOf().copy(currentTerm = currentTermOf(hasSynced = false))

		val current = statisticsHubOf(statistics = statistics).toSummaryStatisticsItem()!!.currentTerm

		assertEquals(UiText.Raw("Sep - Dic 2026"), current.termLink)
		assertEquals(SummaryHubTarget.Record, current.termTarget)
		val content = assertIs<SummaryStatisticsCurrentTermItem.Content.NotSynced>(current.content)
		assertEquals(SummaryHubTarget.Evaluations, content.target)
	}

	@Test
	fun when_theEvaluationsAreDownloadedButTheTermHasNone_then_theBlockSaysSo() {
		val statistics = statisticsOf().copy(currentTerm = currentTermOf(hasSynced = true))

		val current = statisticsHubOf(statistics = statistics).toSummaryStatisticsItem()!!.currentTerm

		assertIs<SummaryStatisticsCurrentTermItem.Content.NoEvaluations>(current.content)
	}

	@Test
	fun when_aTypeHasNoGrade_then_itShowsNoScore_andHasNoTooltip() {
		val statistics = statisticsOf().copy(
			currentTerm = currentTermOf(
				types = listOf(
					evaluationTypeOf(EvaluationType.TEST, earned = 0.0, evaluated = 0.0, graded = 0, total = 4)
				),
				fraction = 0.0
			)
		)

		val data = statisticsHubOf(statistics = statistics)
			.toSummaryStatisticsItem()!!
			.currentTerm
			.content as SummaryStatisticsCurrentTermItem.Content.Data
		val row = data.rows.single()

		assertEquals(UiText.Resource(Res.string.summary_points_none), row.scoreText)
		assertNull(row.tooltip)
	}

	@Test
	fun when_aTypeIsGraded_then_itsTooltipNamesItsPointsAndItsShare() {
		val statistics = statisticsOf().copy(
			currentTerm = currentTermOf(
				types = listOf(
					evaluationTypeOf(EvaluationType.QUIZ, earned = 8.0, evaluated = 10.0, graded = 1, total = 1)
				),
				fraction = 0.025
			)
		)

		val currentTerm = statisticsHubOf(statistics = statistics).toSummaryStatisticsItem()!!.currentTerm
		val row = (currentTerm.content as SummaryStatisticsCurrentTermItem.Content.Data).rows.single()

		assertEquals(UiText.Resource(Res.string.summary_statistics_type_quiz), row.name)
		assertEquals(UiText.Resource(Res.string.summary_statistics_current_graded_one, listOf(1, 1)), row.gradedText)
		assertEquals(
			UiText.Resource(Res.string.summary_statistics_current_score, listOf("8", "10", "80 %")),
			row.scoreText
		)
		assertEquals(
			UiText.Resource(Res.string.summary_statistics_type_tooltip_quiz, listOf("8", "10", "80 %")),
			row.tooltip?.title
		)
	}

	@Test
	fun when_theTermHasSubjectsEvaluated_then_theFigureCountsThePointsOutOfTheirTotal() {
		val statistics = statisticsOf().copy(
			currentTerm = currentTermOf(
				types = listOf(
					evaluationTypeOf(EvaluationType.TEST, earned = 4.0, evaluated = 10.0, graded = 1, total = 2)
				),
				fraction = 0.025
			)
		)
		val hub = statisticsHubOf(
			statistics = statistics,
			term = progressTermOf(10.0, 0.0, null, null)
		)

		val content = hub.toSummaryStatisticsItem()!!.currentTerm.content
		val figure = (content as SummaryStatisticsCurrentTermItem.Content.Data).figure!!

		assertEquals("10", figure.value)
		assertEquals(UiText.Resource(Res.string.summary_statistics_current_figure, listOf(400)), figure.label)
		assertEquals(UiText.Resource(Res.string.summary_career_percent, listOf(3)), figure.trailing)
	}

	@Test
	fun when_evaluationsAreOverdue_then_thatLineLeadsToTheOldestWeek() {
		val attention = ungradedAttentionOf(count = 2)
		val statistics = statisticsOf().copy(
			currentTerm = currentTermOf(types = oneGradelessType(), fraction = 0.0, overdue = 2)
		)
		val hub = statisticsHubOf(statistics = statistics, attention = listOf(attention))

		val content = hub.toSummaryStatisticsItem()!!.currentTerm.content
		val overdue = (content as SummaryStatisticsCurrentTermItem.Content.Data).overdue!!

		assertEquals(UiText.Resource(Res.string.summary_statistics_current_overdue_many, listOf(2)), overdue.text)
		assertEquals(
			SummaryHubTarget.EvaluationsWeek(
				weekStart = LocalDate(2026, 10, 12),
				evaluationId = attention.oldestEvaluationId
			),
			overdue.target
		)
	}

	@Test
	fun when_theHubHasNoOverdueLine_then_theOverdueLinkHasNoTarget() {
		val statistics = statisticsOf().copy(
			currentTerm = currentTermOf(types = oneGradelessType(), fraction = 0.0, overdue = 1)
		)

		val currentTerm = statisticsHubOf(statistics = statistics).toSummaryStatisticsItem()!!.currentTerm
		val overdue = (currentTerm.content as SummaryStatisticsCurrentTermItem.Content.Data).overdue!!

		assertNull(overdue.target)
	}

	// One evaluation type with nothing graded yet: the term has its rows, so the block reads the overdue line.
	private fun oneGradelessType() = listOf(
		evaluationTypeOf(EvaluationType.TEST, earned = 0.0, evaluated = 0.0, graded = 0, total = 1)
	)
}
