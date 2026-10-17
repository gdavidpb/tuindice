package com.gdavidpb.tuindice.assistant.ui.model

import kotlin.test.Test
import kotlin.test.assertEquals

// The numbers of a card are the app's, with the decimal comma of es-VE: an index keeps its four decimals.
class ChatCardNumberTextTest {
	@Test
	fun when_anIndexIsWritten_then_itHasFourDecimalsAndTheComma() {
		assertEquals("3,8125", 3.8125.toCardIndexText())
		assertEquals("3,5000", 3.5.toCardIndexText())
		assertEquals("16,0000", 16.0.toCardIndexText())
	}

	@Test
	fun when_theNumberHasMoreDecimalsThanAreWritten_then_TheyAreCutNotRounded() {
		assertEquals("3,8125", 3.81259.toCardIndexText())
	}

	@Test
	fun when_aNumberHasAnotherCountOfDecimals_then_itKeepsTheComma() {
		assertEquals("16,5", 16.5.toCardDecimalText(1))
		assertEquals("14", 14.0.toCardDecimalText(0))
	}
}
