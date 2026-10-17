package com.gdavidpb.tuindice.about.presentation.machine

/**
 * Internal machine inputs for the about screen: the version load outcome (with the
 * consent flag read alongside), the async uri resolutions that re-enter as
 * effect-carrying symbols, and the assistant observations and write outcome.
 */
sealed interface AboutInternalEvent {
	data class AboutVersionLoaded(
		val versionText: String,
		val usageDataCollectionEnabled: Boolean
	) : AboutInternalEvent

	data class AboutVersionLoadFailed(
		val versionFallbackText: String,
		val usageDataCollectionEnabled: Boolean
	) : AboutInternalEvent

	data class SupportEmailUriLoaded(
		val uri: String
	) : AboutInternalEvent

	data class StoreUriLoaded(
		val uri: String
	) : AboutInternalEvent

	data class AssistantAvailabilityObserved(
		val visible: Boolean
	) : AboutInternalEvent

	data class AssistantConsentObserved(
		val granted: Boolean
	) : AboutInternalEvent

	data class AssistantConsentWriteFailed(
		val restoredGranted: Boolean,
		val message: String
	) : AboutInternalEvent
}
