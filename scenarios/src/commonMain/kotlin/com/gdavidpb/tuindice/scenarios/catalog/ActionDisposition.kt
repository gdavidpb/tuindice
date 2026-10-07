package com.gdavidpb.tuindice.scenarios.catalog

/** Why an MVI action needs no scenario of its own. */
sealed interface ActionDisposition {
	/** `<module>.<Contract>.<Action>`, as `ActionCoverageTest` derives it from the contract sources. */
	val action: String
	val reason: String

	/** The machine fires it itself (bootstrap, observation, renderer callback); no user performs it. */
	data class Internal(override val action: String, override val reason: String) : ActionDisposition

	/** It hands off to the OS (store, mail, picker); a scenario may cover the trigger but not the hand-off. */
	data class PlatformEdge(override val action: String, override val reason: String) : ActionDisposition

	/** A user action that no scenario fires yet: a visible debt, not coverage. */
	data class Pending(override val action: String, override val reason: String) : ActionDisposition
}
