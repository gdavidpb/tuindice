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

	// The rule: a long paste keeps the first seven digits, wherever it lands. With the field full and a part of
	// it selected, a paste of more digits than the selection held pushes the last digits out.
	@Test
	fun when_aLongPasteReplacesAPartOfAFullField_then_keepsTheFirstSevenDigitsAndDropsTheLast() {
		val previous = TextFieldValue(text = "12-34567", selection = TextRange(3, 5))
		val result = value("12-999|567").toMaskedUsbId(previous = previous)

		assertEquals("12-99956", result.text)
		assertEquals(TextRange(6), result.selection)
	}

	@Test
	fun when_aDigitOtherThanTheLastIsDeleted_then_keepsTheDash() {
		assertMasked(previous = "1|2-3", typed = "|2-3", expected = "|23-")
		assertMasked(previous = "1|2-34", typed = "|2-34", expected = "|23-4")
	}

	@Test
	fun when_theDashIsTheOnlyThingDeletedWithTwoDigits_then_dropsIt() {
		assertMasked(previous = "12-|", typed = "12|", expected = "12|")
	}

	@Test
	fun when_aDigitReplacesOnlyTheDashOfAFullField_then_theInsertionIsIgnored() {
		val previous = TextFieldValue(text = "12-34567", selection = TextRange(2, 3))
		val result = value("129|34567").toMaskedUsbId(previous = previous)

		assertEquals("12-34567", result.text)
		assertEquals(TextRange(2, 3), result.selection)
	}

	@Test
	fun when_aDigitReplacesASelectionWithDigitsOfAFullField_then_itIsApplied() {
		val previous = TextFieldValue(text = "12-34567", selection = TextRange(1, 4))
		val result = value("19|4567").toMaskedUsbId(previous = previous)

		assertEquals("19-4567", result.text)
	}

	@Test
	fun when_deleteForwardIsPressedBeforeTheDash_then_deletesTheDigitThatFollowsAndKeepsTheCaret() {
		assertMasked(previous = "12|-345", typed = "12|345", expected = "12|-45")
		assertMasked(previous = "12|-34567", typed = "12|34567", expected = "12|-4567")
		assertMasked(previous = "12|-3", typed = "12|3", expected = "12|-")
	}

	@Test
	fun when_deleteForwardIsPressedBeforeTheDashWithNothingAfterIt_then_dropsTheDashLikeBackspaceDoes() {
		assertMasked(previous = "12|-", typed = "12|", expected = "12|")
	}

	@Test
	fun when_deleteForwardLeavesOnlyTwoDigitsAfterAMidDash_then_theCaretStaysBeforeTheDash() {
		assertMasked(previous = "12|-34", typed = "12|34", expected = "12|-4")
		assertMasked(previous = "12|-3456", typed = "12|3456", expected = "12|-456")
	}

	@Test
	fun when_digitsOfOtherScriptsAreTyped_then_ignoresThem() {
		assertMasked(previous = "|", typed = "\u0661\u0662|", expected = "|")
		assertMasked(previous = "12-|", typed = "12-\u0663|", expected = "12-|")
		assertMasked(previous = "|", typed = "1\u06622\u0663|", expected = "12-|")
	}
}
