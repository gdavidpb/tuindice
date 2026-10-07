package com.gdavidpb.tuindice.auth.ui.view

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

private const val USB_ID_MAX_DIGITS = 7
private const val DASH_INDEX = 2

/**
 * Rebuilds what the user just edited into the USB ID mask `NN-NNNNN`, given the value before the edit.
 *
 * - Only the digits `0`-`9` are kept (not other scripts' digits), at most [USB_ID_MAX_DIGITS]. A longer
 *   input keeps the first seven.
 * - With the field full, an insertion anywhere is ignored: the previous value comes back untouched.
 *   Replacing a selection that holds digits is not an insertion; a selection over only the dash is.
 * - The dash is always present with three or more digits. With exactly two it is present unless the
 *   user has just deleted it (the text had a dash and no longer has it).
 * - The caret stays after the digit that was typed, hopping the dash; it stays before the dash when
 *   the dash was what got deleted.
 * - Delete-forward with the caret right before the dash deletes the digit after it and leaves the caret
 *   in place (the dash is not a character of the value).
 */
internal fun TextFieldValue.toMaskedUsbId(previous: TextFieldValue): TextFieldValue {
	if (isDeleteForwardOverDash(previous)) return previous.withoutDigitAfterDash()

	val allDigits = text.filter(Char::isUsbIdDigit)
	val previousDigitCount = previous.text.count(Char::isUsbIdDigit)
	val isInsertionIntoFullField = previousDigitCount >= USB_ID_MAX_DIGITS &&
		previous.selectedText().none(Char::isUsbIdDigit) &&
		allDigits.length > previousDigitCount

	if (isInsertionIntoFullField) return previous

	val digits = allDigits.take(USB_ID_MAX_DIGITS)
	val isInsertion = digits.length > previousDigitCount
	val keepTrailingDash = '-' in text || '-' !in previous.text
	val masked = when {
		digits.length > DASH_INDEX -> digits.take(DASH_INDEX) + '-' + digits.drop(DASH_INDEX)
		digits.length == DASH_INDEX && keepTrailingDash -> "$digits-"
		else -> digits
	}
	val rawCaret = selection.end.coerceIn(0, text.length)
	val digitsBeforeCaret = text.take(rawCaret).count(Char::isUsbIdDigit).coerceAtMost(digits.length)
	val hopsDash = masked.length > DASH_INDEX && (
		digitsBeforeCaret > DASH_INDEX ||
			(digitsBeforeCaret == DASH_INDEX && (isInsertion || rawCaret > digitsBeforeCaret))
		)
	val caret = (digitsBeforeCaret + if (hopsDash) 1 else 0).coerceAtMost(masked.length)

	return TextFieldValue(text = masked, selection = TextRange(caret))
}

/**
 * Delete-forward with the caret right before the dash and a dash in the middle of the text: the edit
 * removed exactly that dash and left the caret where it was (backspace over the dash leaves the caret
 * one place further, so it is told apart by the previous caret).
 */
private fun TextFieldValue.isDeleteForwardOverDash(previous: TextFieldValue) =
	previous.selection.collapsed &&
		previous.selection.end == DASH_INDEX &&
		previous.text.getOrNull(DASH_INDEX) == '-' &&
		selection.collapsed &&
		selection.end == DASH_INDEX &&
		text == previous.text.removeRange(DASH_INDEX, DASH_INDEX + 1)

/**
 * Deleting forward over the dash deletes the digit that follows it (the dash is not part of the value).
 * The caret stays before the dash; with two digits left the dash stays, as it does after a backspace on
 * the last digit. With no digit after the dash, the dash itself goes, as it does with a backspace.
 */
private fun TextFieldValue.withoutDigitAfterDash(): TextFieldValue {
	val digits = text.filter(Char::isUsbIdDigit)
	val remaining = digits.removeRange(DASH_INDEX, (DASH_INDEX + 1).coerceAtMost(digits.length))
	val masked = when {
		remaining.length > DASH_INDEX -> remaining.take(DASH_INDEX) + '-' + remaining.drop(DASH_INDEX)
		digits.length > DASH_INDEX -> "$remaining-"
		else -> remaining
	}

	return TextFieldValue(text = masked, selection = TextRange(DASH_INDEX))
}

private fun Char.isUsbIdDigit() = this in '0'..'9'

private fun TextFieldValue.selectedText(): String {
	val start = selection.min.coerceIn(0, text.length)
	val end = selection.max.coerceIn(0, text.length)

	return text.substring(start, end)
}
