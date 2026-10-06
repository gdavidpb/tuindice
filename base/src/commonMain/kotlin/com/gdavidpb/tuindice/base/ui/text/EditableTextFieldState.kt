package com.gdavidpb.tuindice.base.ui.text

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

internal const val MAX_REPORTED_TEXTS = 256

/**
 * Holds what a text field shows while a view model owns a `String` copy of it.
 *
 * The rule: the field is the only source of text, selection and IME composition. The view model
 * hears every edit through [edit], but what it echoes back is never allowed to overwrite what the
 * user has typed since. The state machine answers on another dispatcher, so an echo can land
 * between two keystrokes (`a`, `b`, echo `a`, `c` must give `abc`, not `ac`).
 *
 * The holder remembers every text it reported since it last adopted an outside value. Each
 * composition calls [syncExternal] with the caller's current text, before the field reads [value]:
 *
 * - a text the caller already had is not reprocessed (a recomposition for another reason must not
 *   revert the field);
 * - a text equal to the field, or one this holder reported earlier, is an echo or a regression of
 *   the view model and is ignored (so stale, repeated and out-of-order echoes are harmless);
 * - any other text never came from this field (restore, normalisation) and is adopted with the
 *   caret at the end.
 *
 * Blind spot: a caller-originated change to a text the user typed earlier in the same session
 * looks like a stale echo and is ignored. It is closed in two deterministic ways:
 *
 * - `resetKey`: when it changes, the caller's text is adopted unconditionally. Use it for any
 *   input that legitimately restarts the field, such as an identifier mode toggle.
 * - [replace]: the field's own controls (clear button, example chips) change the field locally and
 *   are reported like an edit, so the caller never needs to push those texts back.
 *
 * It is `remember`ed, not `rememberSaveable`: a password must not enter the saved-state bundle.
 * After a configuration change or process death the holder is rebuilt from the view model state.
 *
 * Call sites use `val field = remember { EditableTextFieldState(text, key) }` followed by
 * `field.syncExternal(text, key)`. Whoever owns the state syncs it, once per composition: a field
 * that receives the state only displays and edits it, and never syncs it with another text. If
 * the view model drops an edit, the field keeps showing what was typed until the screen is
 * recreated; that only happens while a screen is leaving its idle state.
 *
 * A5: the state-machine loop stays on `Dispatchers.Default`, so every key still travels to the
 * loop and back as an echo. After this holder nothing the user types waits for that echo: the
 * text, the selection and the IME composition live in the field. What still arrives with the
 * round trip is derived UI only: button enablement (for example the sign-in button) and search
 * gating. The engine is untouched, in line with the accepted trade-off.
 *
 * CMP-7312: the hoisted-state `String` overload of the text fields corrupts input on iOS
 * (`test` becomes `estt`). JetBrains' workaround is the `TextFieldValue` overload with
 * component-local state, which is what this holder feeds, so the design is not exposed by
 * construction. Revisit the `TextFieldState` overloads when Compose Multiplatform 1.13 is stable.
 */
@Stable
class EditableTextFieldState(initialText: String, initialResetKey: Any? = null) {
	var value: TextFieldValue by mutableStateOf(initialText.atEnd())
		private set

	private var externalText = initialText
	private var resetKey = initialResetKey
	private val reportedTexts = LinkedHashSet<String>()

	fun syncExternal(text: String, resetKey: Any? = null) {
		val resetRequested = resetKey != this.resetKey

		this.resetKey = resetKey

		when {
			resetRequested -> adopt(text)
			text == externalText -> Unit
			else -> {
				externalText = text

				if (text != value.text && text !in reportedTexts) adopt(text)
			}
		}
	}

	/** Returns true when the text changed, not only the selection. */
	fun edit(newValue: TextFieldValue): Boolean {
		val textChanged = newValue.text != value.text

		value = newValue

		if (textChanged) report(newValue.text)

		return textChanged
	}

	fun replace(text: String): Boolean = edit(text.atEnd())

	private fun adopt(text: String) {
		externalText = text
		reportedTexts.clear()

		if (value.text != text) value = text.atEnd()
	}

	private fun report(text: String) {
		reportedTexts.remove(text)
		reportedTexts.add(text)

		if (reportedTexts.size > MAX_REPORTED_TEXTS) reportedTexts.remove(reportedTexts.first())
	}
}

private fun String.atEnd() = TextFieldValue(text = this, selection = TextRange(length))
