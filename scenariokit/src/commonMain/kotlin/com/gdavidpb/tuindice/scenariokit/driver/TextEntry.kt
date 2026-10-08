package com.gdavidpb.tuindice.scenariokit.driver

import com.gdavidpb.tuindice.scenariokit.model.Query

/**
 * Text input. The driver never retypes: the interpreter re-reads the field and judges.
 *
 * Every method answers true when the input was delivered. Whether the field then holds the right text is not the
 * driver's call: after `typeKeys` the interpreter polls [ElementProbe.readText] for up to
 * [com.gdavidpb.tuindice.scenariokit.model.Timeouts.TextReread] and fails with both texts when they differ (a
 * secure field is judged by the length of what it shows).
 */
interface TextEntry {
	/**
	 * Types [text] as key events into [q]: the driver focuses the field first by touching it, so the app sees what a
	 * keyboard would produce, and the text goes in at the caret the touch leaves (a scenario that wants an empty field
	 * first uses `replace`, which clears it). Android clicks the field once its bounds are steady and injects the key
	 * events of the virtual keyboard; iOS touches it, waits for the software keyboard (touching a second time if it
	 * does not appear in 2 s) and types in chunks of 4 characters.
	 */
	fun typeKeys(q: Query, text: String): Boolean

	/**
	 * Empties [q]. Android assigns the empty text. iOS selects the content with a triple touch and deletes it, reads
	 * the field again and answers false unless it is empty.
	 */
	fun clearText(q: Query): Boolean

	/**
	 * Sends the IME action of the field that has the focus, as the action key of its keyboard does (Search, Done, Go):
	 * what the app does with it is the app's, and the next step waits for that. It does not choose the field, and it
	 * does not close the keyboard by itself (the app does, or does not). True when the action was sent.
	 */
	fun submitTextEntry(): Boolean

	/**
	 * Puts the on-screen keyboard away without sending the field's action, true when none is showing afterwards.
	 * Android presses back while the keyboard is up. iOS has no such action: its keyboards have no hide key on a phone,
	 * so it answers false while a keyboard is showing (and true when there is none) and says why through
	 * [Diagnostics.lastRefusal]; a scenario that must clear the keyboard on iOS sends the field's action or touches
	 * what the app itself dismisses the keyboard with.
	 */
	fun hideKeyboard(): Boolean
}
