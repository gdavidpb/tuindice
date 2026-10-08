package com.gdavidpb.tuindice.scenariokit.driver

import com.gdavidpb.tuindice.scenariokit.model.Query

/**
 * Text input. The driver never retypes: the interpreter re-reads the field and judges.
 *
 * Every method answers true when the input was delivered. Whether the field then holds the right text is not the
 * driver's call: after `typeKeys` the interpreter polls [ElementProbe.readText] until two reads in a row show the
 * text, for up to [com.gdavidpb.tuindice.scenariokit.model.Timeouts.TextReread], and fails with both texts when they
 * differ (a secure field is judged by the length of what it shows). When the driver answers false the interpreter
 * reads the field too, and [keysInjected] says whether any key went in: a field that holds the beginning of the text
 * is a driver that stopped (`DRIVER_ERROR`), only text nobody sent is a corrupted typing. A refusal carries its
 * reason in [Diagnostics.lastRefusal]. A call that has no app to act on, or no field on screen, answers false.
 */
interface TextEntry {
	/**
	 * Types [text] as key events into [q]: the driver gives the field the focus first, so the app sees what a
	 * keyboard would produce, and the text goes in at the caret (a scenario that wants an empty field first uses
	 * `replace`, which clears it). Android leaves a field that already has the focus alone (it does not wait for a
	 * keyboard then: the keys go to the window that has the focus) and asks one that has not with the accessibility
	 * click action (never with a touch on a point that the opening keyboard could be covering), then waits up to 3 s
	 * for the focus, a listed keyboard and bounds that read the same 3 times; iOS touches the field once (XCUITest does
	 * not report the keyboard focus of a text field). Both read the field before and after the focus and, if it
	 * changed, type nothing and refuse ("the focus request changed the field" on Android, "the focus touch changed
	 * the field" on iOS). Neither driver can prove the focus is on the field it was asked for beyond that.
	 * Android injects the key events of the virtual keyboard, re-stamping each one just before it goes in, stops at
	 * the first one the system refuses and logs how many entered; text the virtual keyboard cannot spell (accents) is
	 * refused. iOS waits up to 2 s for the software keyboard after its touch (it does not touch again: a field that
	 * brings no keyboard is a refusal) and types in chunks of 4 characters through `typeText`.
	 */
	fun typeKeys(q: Query, text: String): Boolean

	/**
	 * How many keys the last [typeKeys] injected (Android counts key events, iOS characters; only zero against
	 * non-zero is read). Zero with an answer of false means the driver stopped before typing: whatever the field then
	 * holds was not put there by the text, and the interpreter does not call it a corrupted typing.
	 */
	fun keysInjected(): Int

	/**
	 * Empties [q] and answers true only when it was read back empty. Android assigns the empty text through an
	 * accessibility action and reads the field for up to 2 s (a label cannot be emptied, so it answers false). iOS
	 * focuses the field, touches it at 75 % of its visible width so the caret is after the text, and presses one delete
	 * key per character it holds, with no second strategy, then reads it again. A text that reaches past that point
	 * leaves the caret in the middle of it, so iOS answers false for it (the reason the secure sample is 25 characters).
	 */
	fun clearText(q: Query): Boolean

	/**
	 * Sends the IME action of the field that has the focus, as the action key of its keyboard does (Search, Done, Go):
	 * what the app does with it is the app's, and the next step waits for that. It does not choose the field, and it
	 * does not close the keyboard by itself (the app does, or does not). True when the action was sent. Android
	 * injects the Enter key, which a single-line field turns into its action; iOS presses the action key of the
	 * keyboard once its frame is still, and refuses when it has no action key (a number pad). Both wait, as a condition
	 * and up to `Timeouts.Action`, because the step can come right after a touch that opens the keyboard, and answer
	 * false with the reason when what they wait for does not come: Android for a keyboard window and a text field with
	 * the focus, iOS only for a keyboard (it cannot read the focus of a field).
	 */
	fun submitTextEntry(): Boolean
}
