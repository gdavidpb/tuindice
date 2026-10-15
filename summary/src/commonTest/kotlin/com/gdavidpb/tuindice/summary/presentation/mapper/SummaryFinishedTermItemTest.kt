package com.gdavidpb.tuindice.summary.presentation.mapper

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.base.utils.extension.formatGrade
import com.gdavidpb.tuindice.summary.domain.model.SummaryFinishedTerm
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.summary_term_credits_value
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SummaryFinishedTermItemTest {
	@Test
	fun when_aTermIsOver_then_ItsNameIsWrittenWithItsIndexOfFourDecimalsAndItsCredits() {
		val item = finishedTerm(index = 4.5714, approvedCredits = 12).toSummaryFinishedTermItem()

		assertEquals(UiText.Raw("${AcademicTermPeriod.APR_JUL.shortLabel} 2026"), item.nameText)
		assertEquals(4.5714.formatGrade(decimals = 4), item.indexText)
		assertEquals(UiText.Resource(Res.string.summary_term_credits_value, listOf(12)), item.creditsText)
	}

	@Test
	fun when_theShareSheetHasACardForTheTerm_then_ItIsShareableUnderItsKey() {
		val shareable = finishedTerm(isShareable = true).toSummaryFinishedTermItem()
		val notShareable = finishedTerm(isShareable = false).toSummaryFinishedTermItem()

		assertEquals("term-key", shareable.termKey)
		assertTrue(shareable.isShareable)
		assertFalse(notShareable.isShareable)
	}

	private fun finishedTerm(
		index: Double = 4.5714,
		approvedCredits: Int = 12,
		isShareable: Boolean = true
	) = SummaryFinishedTerm(
		termKey = "term-key",
		periodCode = AcademicTermPeriod.APR_JUL,
		periodYear = 2026,
		index = index,
		approvedCredits = approvedCredits,
		isShareable = isShareable
	)
}
