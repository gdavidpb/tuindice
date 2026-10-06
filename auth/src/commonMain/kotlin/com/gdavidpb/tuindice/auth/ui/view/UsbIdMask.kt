package com.gdavidpb.tuindice.auth.ui.view

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

private const val USB_ID_MAX_DIGITS = 7
private const val DASH_INDEX = 2

/**
 * Rebuilds what the user just edited into the USB ID mask `NN-NNNNN`, given the value before the edit.
 *
 * - Only digits are kept, at most [USB_ID_MAX_DIGITS]. A longer input keeps the first seven.
 * - With the field full, an insertion anywhere is ignored: the previous value comes back untouched.
 *   A selection that is replaced is not an insertion.
 * - The dash is always present with three or more digits. With exactly two it is present unless the
 *   user has just deleted it.
 * - The caret stays after the digit that was typed, hopping the dash; it stays before the dash when
 *   the dash was what got deleted.
 */
internal fun TextFieldValue.toMaskedUsbId(previous: TextFieldValue): TextFieldValue {
	val allDigits = text.filter(Char::isDigit)
	val previousDigitCount = previous.text.count(Char::isDigit)
	val isInsertionIntoFullField = previousDigitCount >= USB_ID_MAX_DIGITS &&
		previous.selection.collapsed &&
		allDigits.length > previousDigitCount

	if (isInsertionIntoFullField) return previous

	val digits = allDigits.take(USB_ID_MAX_DIGITS)
	val isInsertion = digits.length > previousDigitCount
	val keepTrailingDash = text.getOrNull(DASH_INDEX) == '-' || previous.text.getOrNull(DASH_INDEX) != '-'
	val masked = when {
		digits.length > DASH_INDEX -> digits.take(DASH_INDEX) + '-' + digits.drop(DASH_INDEX)
		digits.length == DASH_INDEX && keepTrailingDash -> "$digits-"
		else -> digits
	}
	val rawCaret = selection.end.coerceIn(0, text.length)
	val digitsBeforeCaret = text.take(rawCaret).count(Char::isDigit).coerceAtMost(digits.length)
	val hopsDash = masked.length > DASH_INDEX && (
		digitsBeforeCaret > DASH_INDEX ||
			(digitsBeforeCaret == DASH_INDEX && (isInsertion || rawCaret > digitsBeforeCaret))
		)
	val caret = (digitsBeforeCaret + if (hopsDash) 1 else 0).coerceAtMost(masked.length)

	return TextFieldValue(text = masked, selection = TextRange(caret))
}
