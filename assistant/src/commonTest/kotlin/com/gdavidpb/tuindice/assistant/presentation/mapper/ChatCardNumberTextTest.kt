package com.gdavidpb.tuindice.assistant.presentation.mapper

import kotlin.test.Test
import kotlin.test.assertEquals

// The numbers of a card are the app's, with the decimal point, as the rest of the app: an index keeps its four
// decimals.
class ChatCardNumberTextTest {
	@Test
	fun when_anIndexIsWritten_then_itHasFourDecimalsAndThePoint() {
		assertEquals("3.8125", 3.8125.toCardIndexText())
		assertEquals("3.5000", 3.5.toCardIndexText())
		assertEquals("16.0000", 16.0.toCardIndexText())
	}

	@Test
	fun when_theNumberHasMoreDecimalsThanAreWritten_then_TheyAreCutNotRounded() {
		assertEquals("3.8125", 3.81259.toCardIndexText())
	}

	@Test
	fun when_aNumberHasAnotherCountOfDecimals_then_itKeepsThePoint() {
		assertEquals("16.5", 16.5.toCardDecimalText(1))
		assertEquals("14", 14.0.toCardDecimalText(0))
	}
}
