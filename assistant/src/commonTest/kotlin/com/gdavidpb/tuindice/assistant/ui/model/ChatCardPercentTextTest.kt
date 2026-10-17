package com.gdavidpb.tuindice.assistant.ui.model

import com.gdavidpb.tuindice.base.presentation.model.UiText
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_card_b_percent
import kotlin.test.Test
import kotlin.test.assertEquals

// Percentages are whole numbers with their sign after a space, and a bar fills only the part of its whole.
class ChatCardPercentTextTest {
	@Test
	fun when_aPercentageIsWritten_then_itHasItsSignAfterASpace() {
		assertEquals(UiText.Resource(Res.string.assistant_card_b_percent, listOf(47, "%")), chatCardPercentText(47))
	}

	@Test
	fun when_aPartOfAWholeIsCounted_then_itIsTheNearestWholePercent_andAnEmptyWholeIsZero() {
		assertEquals(47, chatCardPercentOf(part = 70, whole = 150))
		assertEquals(0, chatCardPercentOf(part = 5, whole = 0))
		assertEquals(72, 0.72.toCardPercent())
	}

	@Test
	fun when_aBarIsFilled_then_itTakesThePartOfTheWholeAndNeverMore() {
		assertEquals(0.5f, chatCardFraction(part = 5.0, whole = 10.0))
		assertEquals(0f, chatCardFraction(part = 5.0, whole = 0.0))
		assertEquals(1f, chatCardFraction(part = 12.0, whole = 10.0))
	}
}
