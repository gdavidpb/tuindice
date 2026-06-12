package com.gdavidpb.tuindice.about.presentation.machine

/**
 * Internal machine inputs for the about screen: the version load outcome (with the
 * consent flag read alongside) and the async uri resolutions that re-enter as
 * effect-carrying symbols.
 */
sealed interface AboutInternalEvent {
	data class AboutVersionLoaded(
		val versionText: String,
		val usageDataCollectionEnabled: Boolean
	) : AboutInternalEvent

	data object AboutVersionLoadFailed : AboutInternalEvent

	data class SupportEmailUriLoaded(
		val uri: String
	) : AboutInternalEvent

	data class StoreUriLoaded(
		val uri: String
	) : AboutInternalEvent
}
