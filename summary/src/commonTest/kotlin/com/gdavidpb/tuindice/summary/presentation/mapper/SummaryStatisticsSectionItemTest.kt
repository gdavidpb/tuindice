package com.gdavidpb.tuindice.summary.presentation.mapper

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.academiccore.domain.model.SubjectOutcomes
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.summary.domain.model.SummaryHub
import com.gdavidpb.tuindice.summary.domain.model.SummaryTerm
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsSectionItem
import com.gdavidpb.tuindice.summary.testing.currentTermOf
import com.gdavidpb.tuindice.summary.testing.progressTermOf
import com.gdavidpb.tuindice.summary.testing.statisticsHubOf
import com.gdavidpb.tuindice.summary.testing.statisticsOf
import com.gdavidpb.tuindice.summary.testing.statisticsTerm
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.summary_statistics_section_approved_best_many
import tuindice.summary.generated.resources.summary_statistics_section_approved_best_one
import tuindice.summary.generated.resources.summary_statistics_subjects_approved_many
import tuindice.summary.generated.resources.summary_statistics_subjects_approved_one
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SummaryStatisticsSectionItemTest {
	@Test
	fun when_theRecordIsNotReady_then_thereIsNoSection() {
		val hub = SummaryHub(attention = emptyList(), week = null, term = null, career = null, statistics = null)

		assertNull(hub.toSummaryStatisticsSectionItem())
		assertNull(hub.toSummaryHubItem().statistics)
	}

	@Test
	fun when_theHubIsMapped_then_theItemCarriesTheSectionOfItsStatistics() {
		val hub = statisticsHubOf(
			statistics = statisticsOf(),
			term = SummaryTerm.Idle(hasTerms = false, finished = null)
		)

		assertEquals(SummaryStatisticsSectionItem.Empty, hub.toSummaryHubItem().statistics)
	}

	@Test
	fun when_aNewStudentHasNoTermYet_then_theSectionSaysWhatItWillShowAndHasNoLink() {
		val hub = statisticsHubOf(
			statistics = statisticsOf(),
			term = SummaryTerm.Idle(hasTerms = false, finished = null)
		)

		assertEquals(SummaryStatisticsSectionItem.Empty, hub.toSummaryStatisticsSectionItem())
	}

	@Test
	fun when_theRecordHasOnlyPlannedTerms_then_theSectionSaysWhatItWillShowAndHasNoLink() {
		val hub = statisticsHubOf(
			statistics = statisticsOf(),
			term = SummaryTerm.Idle(hasTerms = true, finished = null)
		)

		assertEquals(SummaryStatisticsSectionItem.Empty, hub.toSummaryStatisticsSectionItem())
	}

	@Test
	fun when_aTermIsInProgressAndNoneIsOver_then_theSectionInvitesToReadTheStatistics() {
		val hub = statisticsHubOf(
			statistics = statisticsOf().copy(currentTerm = currentTermOf()),
			term = progressTermOf(0.5)
		)

		assertEquals(SummaryStatisticsSectionItem.Invite, hub.toSummaryStatisticsSectionItem())
	}

	@Test
	fun when_termsAreOver_then_theFigureIsTheIndexOfTheScreenAndThePhraseNamesTheBestTerm() {
		val first = statisticsTerm(year = 2023, periodCode = AcademicTermPeriod.SEP_DEC, index = 4.0)
		val second = statisticsTerm(year = 2024, periodCode = AcademicTermPeriod.JAN_MAR, index = 4.5)
			.copy(cumulativeIndex = 4.25)
		val hub = statisticsHubOf(
			statistics = statisticsOf(terms = listOf(first, second), subjects = subjectsApproved(44))
		)

		assertEquals(
			SummaryStatisticsSectionItem.Figures(
				figure = "4.2500",
				line = listOf(4.0f, 4.25f),
				phrase = UiText.Resource(
					Res.string.summary_statistics_section_approved_best_many,
					listOf(44, "Ene - Mar 2024")
				)
			),
			hub.toSummaryStatisticsSectionItem()
		)
	}

	@Test
	fun when_oneSubjectIsApprovedAndATermHasABestOne_then_thePhraseIsInTheSingular() {
		val term = statisticsTerm(year = 2024, periodCode = AcademicTermPeriod.JAN_MAR, index = 4.0)
		val hub = statisticsHubOf(statistics = statisticsOf(terms = listOf(term), subjects = subjectsApproved(1)))

		assertEquals(
			SummaryStatisticsSectionItem.Figures(
				figure = "4.0000",
				line = listOf(4.0f),
				phrase = UiText.Resource(
					Res.string.summary_statistics_section_approved_best_one,
					listOf(1, "Ene - Mar 2024")
				)
			),
			hub.toSummaryStatisticsSectionItem()
		)
	}

	@Test
	fun when_theOnlyTermOverIsJulyAugust_then_thePhraseCountsTheSubjectsWithoutABestTerm() {
		val summer = statisticsTerm(year = 2024, periodCode = AcademicTermPeriod.JUL_AUG, index = 4.0)
		val hub = statisticsHubOf(statistics = statisticsOf(terms = listOf(summer), subjects = subjectsApproved(1)))

		assertEquals(
			SummaryStatisticsSectionItem.Figures(
				figure = "4.0000",
				line = listOf(4.0f),
				phrase = UiText.Resource(Res.string.summary_statistics_subjects_approved_one, listOf(1))
			),
			hub.toSummaryStatisticsSectionItem()
		)
	}

	@Test
	fun when_theTermsOverHaveNoAverage_then_thereIsNoFigureNorLineAndTheCountRemains() {
		val ungraded = statisticsTerm(year = 2024, periodCode = AcademicTermPeriod.JAN_MAR, index = null)
		val hub = statisticsHubOf(statistics = statisticsOf(terms = listOf(ungraded), subjects = subjectsApproved(2)))

		assertEquals(
			SummaryStatisticsSectionItem.Figures(
				figure = null,
				line = emptyList(),
				phrase = UiText.Resource(Res.string.summary_statistics_subjects_approved_many, listOf(2))
			),
			hub.toSummaryStatisticsSectionItem()
		)
	}

	@Test
	fun when_aTermIsOverWhileAnotherIsInProgress_then_theFiguresShowRatherThanTheInvitation() {
		val closed = statisticsTerm(year = 2024, periodCode = AcademicTermPeriod.JAN_MAR, index = 4.0)
		val hub = statisticsHubOf(
			statistics = statisticsOf(terms = listOf(closed), subjects = subjectsApproved(3))
				.copy(currentTerm = currentTermOf()),
			term = progressTermOf(0.5)
		)

		assertEquals(
			SummaryStatisticsSectionItem.Figures(
				figure = "4.0000",
				line = listOf(4.0f),
				phrase = UiText.Resource(
					Res.string.summary_statistics_section_approved_best_many,
					listOf(3, "Ene - Mar 2024")
				)
			),
			hub.toSummaryStatisticsSectionItem()
		)
	}

	private fun subjectsApproved(approved: Int): SubjectOutcomes {
		return SubjectOutcomes(
			approved = approved,
			failed = 0,
			withdrawn = 0,
			noResult = 0,
			approvedFirstTry = 0,
			firstTryBase = 0,
			repeated = 0
		)
	}
}
