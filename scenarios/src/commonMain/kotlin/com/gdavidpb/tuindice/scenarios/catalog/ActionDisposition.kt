package com.gdavidpb.tuindice.scenarios.catalog

/**
 * Why an MVI action needs no scenario of its own. An action is either covered by a scenario or dispositioned,
 * never both (`ActionCoverageTest`): when a scenario starts covering it, its disposition is deleted.
 */
sealed interface ActionDisposition {
	/** `<module>.<Contract>.<Action>`, as `ActionCoverageTest` derives it from the contract sources. */
	val action: String
	val reason: String

	/** The machine fires it itself (bootstrap, observation, renderer callback); no user performs it. */
	data class Internal(override val action: String, override val reason: String) : ActionDisposition

	/** It hands off to the OS (store, system prompt, file source) in a way no scenario carries out or checks. */
	data class PlatformEdge(override val action: String, override val reason: String) : ActionDisposition

	/** A user action that no scenario fires yet: a visible debt, not coverage. */
	data class Pending(override val action: String, override val reason: String) : ActionDisposition
}
