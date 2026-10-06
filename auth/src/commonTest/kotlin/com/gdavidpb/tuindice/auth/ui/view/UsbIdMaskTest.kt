package com.gdavidpb.tuindice.auth.ui.view

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import kotlin.test.Test
import kotlin.test.assertEquals

class UsbIdMaskTest {
	// `|` marks the caret; the mask never receives a range selection except where a test says so.
	private fun value(marked: String): TextFieldValue {
		val caret = marked.indexOf('|')

		return TextFieldValue(
			text = marked.replace("|", ""),
			selection = TextRange(caret)
		)
	}

	private fun assertMasked(previous: String, typed: String, expected: String) {
		val result = value(typed).toMaskedUsbId(previous = value(previous))

		assertEquals(expected.replace("|", ""), result.text)
		assertEquals(expected.indexOf('|'), result.selection.end)
		assertEquals(result.selection.start, result.selection.end)
	}

	@Test
	fun when_digitsAreTypedFromEmpty_then_addsTheDashAndHopsIt() {
		assertMasked(previous = "|", typed = "1|", expected = "1|")
		assertMasked(previous = "1|", typed = "12|", expected = "12-|")
		assertMasked(previous = "12-|", typed = "12-3|", expected = "12-3|")
	}

	@Test
	fun when_backspaceRemovesTheLastDigitAfterTheDash_then_keepsTheDash() {
		assertMasked(previous = "12-3|", typed = "12-|", expected = "12-|")
	}

	@Test
	fun when_backspaceRemovesTheTrailingDash_then_dropsIt() {
		assertMasked(previous = "12-|", typed = "12|", expected = "12|")
	}

	@Test
	fun when_backspaceRemovesTheDashInTheMiddle_then_restoresItAfterTheCaret() {
		assertMasked(previous = "12-|345", typed = "12|345", expected = "12|-345")
	}

	@Test
	fun when_aLetterIsTyped_then_ignoresIt() {
		assertMasked(previous = "12-|", typed = "12-a|", expected = "12-|")
	}

	@Test
	fun when_lettersAreMixedWithDigits_then_keepsOnlyTheDigits() {
		assertMasked(previous = "|", typed = "ab12c3|", expected = "12-3|")
	}

	@Test
	fun when_moreThanSevenDigitsArePasted_then_keepsTheFirstSeven() {
		assertMasked(previous = "|", typed = "1234567890|", expected = "12-34567|")
	}

	@Test
	fun when_theTextIsPastedWithSpaces_then_trimsThem() {
		assertMasked(previous = "|", typed = " 12-34567 |", expected = "12-34567|")
	}

	@Test
	fun when_aDigitIsInsertedIntoAFullFieldAtTheEnd_then_changesNothing() {
		assertMasked(previous = "12-34567|", typed = "12-345679|", expected = "12-34567|")
	}

	@Test
	fun when_aDigitIsInsertedIntoAFullFieldInTheMiddle_then_changesNothing() {
		assertMasked(previous = "12-3|4567", typed = "12-39|4567", expected = "12-3|4567")
	}

	@Test
	fun when_aDigitIsInsertedIntoAFieldThatIsNotFull_then_shiftsTheDigitsAndKeepsTheCaretAfterIt() {
		assertMasked(previous = "12-3|45", typed = "12-39|45", expected = "12-39|45")
	}

	@Test
	fun when_aFullSelectionIsReplacedByALongPaste_then_keepsTheFirstSevenOfThePaste() {
		val previous = TextFieldValue(text = "12-34567", selection = TextRange(0, 8))
		val result = value("9876543210|").toMaskedUsbId(previous = previous)

		assertEquals("98-76543", result.text)
		assertEquals(TextRange(8), result.selection)
	}
}
