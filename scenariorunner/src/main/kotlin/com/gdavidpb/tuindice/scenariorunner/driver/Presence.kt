package com.gdavidpb.tuindice.scenariorunner.driver

/** What one read of the app's window says about a query; a screen that could not be read proves nothing. */
internal enum class Presence {
	/** The query matched a node. */
	PRESENT,

	/** The app's window was read and nothing matched. */
	ABSENT,

	/** The accessibility tree failed or had no root for the app, so neither "there" nor "gone" is known. */
	UNREADABLE
}
