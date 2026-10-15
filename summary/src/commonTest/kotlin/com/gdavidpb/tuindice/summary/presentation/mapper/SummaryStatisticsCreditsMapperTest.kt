package com.gdavidpb.tuindice.summary.presentation.mapper

import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.summary.domain.model.SummaryCreditsProgress
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsCreditsItem
import com.gdavidpb.tuindice.summary.testing.statisticsTerm
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.summary_career_credits_rest
import tuindice.summary.generated.resources.summary_career_description
import tuindice.summary.generated.resources.summary_career_percent
import tuindice.summary.generated.resources.summary_statistics_credits_empty
import tuindice.summary.generated.resources.summary_statistics_credits_invite_message
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class SummaryStatisticsCreditsMapperTest {
	@Test
	fun when_noTermIsClosed_then_theBlockSaysWhenItsCreditsAppear() {
		val item = creditsItemOf(
			closedTerms = emptyList(),
			pensum = SummaryCreditsProgress(approvedCredits = 152, totalCredits = 205)
		)

		assertEquals(
			SummaryStatisticsCreditsItem.Content.Empty(UiText.Resource(Res.string.summary_statistics_credits_empty)),
			item.content
		)
	}

	@Test
	fun when_noPensumIsCached_then_theBlockInvitesToOpenIt() {
		val item = creditsItemOf(closedTerms = listOf(statisticsTerm(year = 2024)), pensum = null)

		assertEquals(
			SummaryStatisticsCreditsItem.Content.Empty(
				UiText.Resource(Res.string.summary_statistics_credits_invite_message)
			),
			item.content
		)
	}

	@Test
	fun when_thePensumHasNoCredits_then_theBlockInvitesToOpenIt() {
		val item = creditsItemOf(
			closedTerms = listOf(statisticsTerm(year = 2024)),
			pensum = SummaryCreditsProgress(approvedCredits = 0, totalCredits = 0)
		)

		assertEquals(
			SummaryStatisticsCreditsItem.Content.Empty(
				UiText.Resource(Res.string.summary_statistics_credits_invite_message)
			),
			item.content
		)
	}

	@Test
	fun when_aPensumIsCached_then_itsApprovedCreditsAreTheFigureWithTheirShare() {
		val item = creditsItemOf(
			closedTerms = listOf(statisticsTerm(year = 2024)),
			pensum = SummaryCreditsProgress(approvedCredits = 152, totalCredits = 205)
		)

		val data = assertIs<SummaryStatisticsCreditsItem.Content.Data>(item.content)
		assertEquals("152", data.figure.value)
		assertEquals(UiText.Resource(Res.string.summary_career_credits_rest, listOf(205)), data.figure.label)
		assertEquals(UiText.Resource(Res.string.summary_career_percent, listOf(74)), data.figure.trailing)
	}

	@Test
	fun when_aPensumIsCached_then_theBarFollowsItAndIsDescribedAsTheCareerCardIs() {
		val item = creditsItemOf(
			closedTerms = listOf(statisticsTerm(year = 2024)),
			pensum = SummaryCreditsProgress(approvedCredits = 152, totalCredits = 205)
		)

		val data = assertIs<SummaryStatisticsCreditsItem.Content.Data>(item.content)
		assertEquals(152f / 205f, data.fraction, 0.0001f)
		assertEquals(UiText.Resource(Res.string.summary_career_description, listOf(152, 205, 74)), data.description)
	}
}
