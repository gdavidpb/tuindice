package com.gdavidpb.tuindice.about.presentation.machine

/**
 * Internal machine inputs for the about screen: the version load outcome and the async uri resolution
 * that re-enters as an effect-carrying symbol.
 */
sealed interface AboutInternalEvent {
	data class AboutVersionLoaded(
		val versionText: String
	) : AboutInternalEvent

	data class AboutVersionLoadFailed(
		val versionFallbackText: String
	) : AboutInternalEvent

	data class ContactEmailUriLoaded(
		val uri: String
	) : AboutInternalEvent
}
