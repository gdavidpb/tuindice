package com.gdavidpb.tuindice.scenariokit.driver

import com.gdavidpb.tuindice.scenariokit.model.Query

/**
 * Text input. The driver never retypes: the interpreter re-reads the field and judges.
 *
 * Every method answers true when the input was delivered. Whether the field then holds the right text is not the
 * driver's call: after `typeKeys` the interpreter polls [ElementProbe.readText] until two reads in a row show the
 * text, for up to [com.gdavidpb.tuindice.scenariokit.model.Timeouts.TextReread], and fails with both texts when they
 * differ (a secure field is judged by the length of what it shows). When the driver answers false the interpreter
 * reads the field too: part of the text in it is a corrupted typing, not a refusal. A refusal carries its reason in
 * [Diagnostics.lastRefusal]. A call that has no app to act on, or no field on screen, answers false.
 */
interface TextEntry {
	/**
	 * Types [text] as key events into [q]: the driver focuses the field first by touching it once its bounds are
	 * steady (and not under the keyboard), so the app sees what a keyboard would produce, and the text goes in at the
	 * caret the touch leaves (a scenario that wants an empty field first uses `replace`, which clears it). Android
	 * injects the key events of the virtual keyboard, re-stamping each one just before it goes in, stops at the first
	 * one the system refuses and logs how many entered; text the virtual keyboard cannot spell (accents) is refused.
	 * iOS touches the field once, waits up to 2 s for the software keyboard (it does not touch again: a field that
	 * brings no keyboard is a refusal) and types in chunks of 4 characters through `typeText`.
	 */
	fun typeKeys(q: Query, text: String): Boolean

	/**
	 * Empties [q] and answers true only when it was read back empty. Android assigns the empty text through an
	 * accessibility action and reads the field for up to 2 s (a label cannot be emptied, so it answers false). iOS
	 * focuses the field, touches near its right end so the caret is after the text, and presses one delete key per
	 * character it holds, with no touch on the element and no second strategy, then reads it again.
	 */
	fun clearText(q: Query): Boolean

	/**
	 * Sends the IME action of the field that has the focus, as the action key of its keyboard does (Search, Done, Go):
	 * what the app does with it is the app's, and the next step waits for that. It does not choose the field, and it
	 * does not close the keyboard by itself (the app does, or does not). True when the action was sent. Android
	 * injects the Enter key, which a single-line field turns into its action; iOS presses the action key of the
	 * keyboard once its frame is still, and refuses when there is no keyboard or it has no action key (a number pad).
	 */
	fun submitTextEntry(): Boolean
}
