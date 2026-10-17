package com.gdavidpb.tuindice.assistant.ui.model

import com.gdavidpb.tuindice.base.presentation.model.UiText
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_card_b_count_evaluation_many
import tuindice.assistant.generated.resources.assistant_card_b_count_evaluation_one
import tuindice.assistant.generated.resources.assistant_card_b_points_many
import tuindice.assistant.generated.resources.assistant_card_b_points_one
import kotlin.test.Test
import kotlin.test.assertEquals

// Grades, points and counts read as the Evaluations tab reads them, with the decimal comma and the noun in its number.
class ChatCardQuantityTextTest {
	@Test
	fun when_aNumberIsWhole_then_itHasNoDecimals() {
		assertEquals("14", 14.0.toCardCompactNumberText())
	}

	@Test
	fun when_aNumberHasDecimals_then_itHasTwoWithTheComma_andTheDigitsPastThemAreCut() {
		assertEquals("12,50", 12.5.toCardCompactNumberText())
		assertEquals("12,34", 12.345.toCardCompactNumberText())
	}

	@Test
	fun when_aCountIsOne_then_itTakesTheSingularNoun() {
		val one = Res.string.assistant_card_b_count_evaluation_one
		val many = Res.string.assistant_card_b_count_evaluation_many

		assertEquals(UiText.Resource(one, listOf(1)), chatCardCountText(count = 1, one = one, many = many))
		assertEquals(UiText.Resource(many, listOf(3)), chatCardCountText(count = 3, one = one, many = many))
	}

	@Test
	fun when_pointsAreOne_then_theyTakeTheSingularNoun() {
		assertEquals(UiText.Resource(Res.string.assistant_card_b_points_one, listOf("1")), chatCardPointsText(1.0))
		assertEquals(UiText.Resource(Res.string.assistant_card_b_points_many, listOf("12,50")), chatCardPointsText(12.5))
	}
}
