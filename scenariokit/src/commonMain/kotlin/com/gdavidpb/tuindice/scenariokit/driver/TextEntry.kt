package com.gdavidpb.tuindice.scenariokit.driver

import com.gdavidpb.tuindice.scenariokit.model.Query

/** Text input. The driver never retypes: the interpreter re-reads the field and judges. */
interface TextEntry {
	/** Types [text] as key events into [q]. */
	fun typeKeys(q: Query, text: String): Boolean

	/** Assigns [text] to [q] in one operation. */
	fun setText(q: Query, text: String): Boolean

	fun clearText(q: Query): Boolean

	/** Dismisses the keyboard if one is showing. */
	fun finishTextEntry(): Boolean
}
