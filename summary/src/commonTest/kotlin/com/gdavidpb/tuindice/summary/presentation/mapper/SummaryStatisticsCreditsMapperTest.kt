package com.gdavidpb.tuindice.summary.presentation.mapper

import com.gdavidpb.tuindice.academiccore.domain.model.CreditOutcomes
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.summary.domain.model.SummaryCreditsProgress
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsCreditsItem
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.summary_career_percent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SummaryStatisticsCreditsMapperTest {
	@Test
	fun when_noCreditIsFromTheRecord_then_theBlockSaysWhatItLacks() {
		val item = creditsItemOf(CreditOutcomes(approved = 0, failed = 0, withdrawn = 0), pensum = null)

		assertTrue(item.content is SummaryStatisticsCreditsItem.Content.Empty)
	}

	@Test
	fun when_theRecordHasCredits_then_TheBarHoldsEachOutcome() {
		val data = dataOf(creditsItemOf(CreditOutcomes(approved = 120, failed = 8, withdrawn = 4), pensum = null))

		assertEquals("132", data.figure.value)
		assertEquals(listOf(120f, 8f, 4f), data.segments.map { segment -> segment.value })
		assertNull(data.pensum)
	}

	@Test
	fun when_aPensumIsCached_then_ItsProgressFollowsTheBarWithItsShare() {
		val progress = SummaryCreditsProgress(approvedCredits = 152, totalCredits = 205)

		val pensum = dataOf(creditsItemOf(CreditOutcomes(approved = 152, failed = 0, withdrawn = 0), progress)).pensum

		assertEquals("152", pensum?.figure?.value)
		assertEquals(UiText.Resource(Res.string.summary_career_percent, listOf(74)), pensum?.figure?.trailing)
		assertEquals(152f / 205f, pensum?.fraction ?: 0f, 0.0001f)
	}

	@Test
	fun when_thePensumHasNoCredits_then_TheProgressIsLeftOut() {
		val progress = SummaryCreditsProgress(approvedCredits = 0, totalCredits = 0)

		val data = dataOf(creditsItemOf(CreditOutcomes(approved = 3, failed = 0, withdrawn = 0), progress))

		assertNull(data.pensum)
	}

	private fun dataOf(item: SummaryStatisticsCreditsItem): SummaryStatisticsCreditsItem.Content.Data {
		return item.content as SummaryStatisticsCreditsItem.Content.Data
	}
}
