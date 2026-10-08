package com.gdavidpb.tuindice.scenariokit.driver

import com.gdavidpb.tuindice.scenariokit.model.Query

/**
 * Text input. The driver never retypes: the interpreter re-reads the field and judges.
 *
 * Every method answers true when the input was delivered. Whether the field then holds the right text is not the
 * driver's call: after `typeKeys` or `setText` the interpreter polls [ElementProbe.readText] for up to
 * [com.gdavidpb.tuindice.scenariokit.model.Timeouts.TextReread] and fails with both texts when they differ, except
 * for secure fields, which cannot be read back.
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
	 * Assigns [text] to [q] in one operation, replacing what it holds. Android sets the text through an accessibility
	 * action. iOS pastes it through the edit menu of the focused field, which offers only "paste" on an empty field,
	 * so on iOS it works on an empty field; no scenario uses `TextEntryMode.Set` today.
	 */
	fun setText(q: Query, text: String): Boolean

	/**
	 * Empties [q]. Android assigns the empty text. iOS selects the content with a triple touch and deletes it, reads
	 * the field again and answers false unless it is empty.
	 */
	fun clearText(q: Query): Boolean

	/** Dismisses the keyboard if one is showing. */
	fun finishTextEntry(): Boolean
}
