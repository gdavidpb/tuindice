package com.gdavidpb.tuindice.base.ui.text

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EditableTextFieldStateTest {
	private fun EditableTextFieldState.type(text: String) = edit(TextFieldValue(text, TextRange(text.length)))

	@Test
	fun staleEchoOfAnEarlierEdit_isIgnored() {
		val field = EditableTextFieldState("")

		field.type("a")
		field.type("ab")
		field.syncExternal("a")

		assertEquals("ab", field.value.text)

		field.type("abc")

		assertEquals("abc", field.value.text)
	}

	@Test
	fun echoesOfRepeatedTexts_areIgnoredInAnyOrder() {
		val orders = listOf(
			listOf("a", "ab", "a"),
			listOf("ab", "a", "ab"),
			listOf("ab", "a"),
			listOf("a", "ab")
		)

		orders.forEach { echoes ->
			val field = EditableTextFieldState("")

			field.type("a")
			field.type("ab")
			field.type("a")

			echoes.forEach { echo ->
				field.syncExternal(echo)

				assertEquals("a", field.value.text, "echo '$echo' of $echoes")
			}
		}
	}

	@Test
	fun textTheFieldNeverReported_isAdoptedWithTheCaretAtTheEnd() {
		val field = EditableTextFieldState("")

		field.type("a")
		field.syncExternal("restored")

		assertEquals("restored", field.value.text)
		assertEquals(TextRange("restored".length), field.value.selection)
	}

	@Test
	fun unchangedCallerText_isNotReprocessedAfterALocalEdit() {
		val field = EditableTextFieldState("")

		field.syncExternal("x")
		field.type("xy")
		field.syncExternal("x")

		assertEquals("xy", field.value.text)
	}

	@Test
	fun callerFallingBackToAnEarlierReportedText_isIgnored() {
		val field = EditableTextFieldState("")

		field.type("calculo")
		field.type("calculo ")
		field.syncExternal("calculo ")
		field.syncExternal("calculo")

		assertEquals("calculo ", field.value.text)

		field.type("calculo 2")

		assertEquals("calculo 2", field.value.text)
	}

	@Test
	fun resetKeyChange_adoptsEvenAReportedText() {
		val field = EditableTextFieldState("", initialResetKey = "mode-a")

		field.type("a")
		field.type("ab")
		field.syncExternal("a", resetKey = "mode-b")

		assertEquals("a", field.value.text)
		assertEquals(TextRange(1), field.value.selection)
	}

	@Test
	fun replace_isReportedLikeAnEdit() {
		val field = EditableTextFieldState("")

		field.type("a")

		assertTrue(field.replace("b"))
		assertEquals(TextRange(1), field.value.selection)

		field.syncExternal("a")

		assertEquals("b", field.value.text)

		field.syncExternal("b")

		assertEquals("b", field.value.text)
		assertFalse(field.replace("b"))
	}

	@Test
	fun selectionOnlyEdit_returnsFalseAndKeepsTheSelection() {
		val field = EditableTextFieldState("abc")

		val changed = field.edit(TextFieldValue("abc", TextRange(1)))

		assertFalse(changed)
		assertEquals(TextRange(1), field.value.selection)
	}

	@Test
	fun ledgerDropsItsOldestTextBeyondTheBound() {
		val field = EditableTextFieldState("")
		val texts = (0..MAX_REPORTED_TEXTS).map { "t$it" }

		texts.forEach { field.type(it) }

		field.syncExternal(texts.last())

		assertEquals(texts.last(), field.value.text)

		field.syncExternal(texts[1])

		assertEquals(texts.last(), field.value.text, "a recent text is still remembered")

		field.syncExternal(texts.first())

		assertEquals(texts.first(), field.value.text, "the oldest text was dropped from the ledger")
	}
}
