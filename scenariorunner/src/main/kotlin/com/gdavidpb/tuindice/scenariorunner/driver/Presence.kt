package com.gdavidpb.tuindice.scenariorunner.driver

/** What one read of the app's window says about a query; a screen that could not be read proves nothing. */
internal enum class Presence {
	/** The query matched a node. */
	PRESENT,

	/** The app's window was read and nothing matched. */
	ABSENT,

	/** The accessibility tree failed or had no root for the app, so neither "there" nor "gone" is known. */
	UNREADABLE;

	companion object {
		/**
		 * The decision, on what the two reads of a pass found. [matched] is whether a node matched the query, null when the
		 * read threw; [appWindowInTree] is whether the app's own window was in the tree in the same pass, null when that read
		 * threw (it is only asked when nothing matched). ABSENT needs both reads to have worked: nothing matched and the
		 * app's window was there to be matched in.
		 */
		fun of(matched: Boolean?, appWindowInTree: Boolean?): Presence = when {
			matched == null -> UNREADABLE
			matched -> PRESENT
			appWindowInTree == true -> ABSENT
			else -> UNREADABLE
		}
	}
}
