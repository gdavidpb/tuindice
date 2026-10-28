package com.gdavidpb.tuindice.about.presentation.machine

import com.gdavidpb.tuindice.base.domain.model.ProfileIdentity

/**
 * Internal machine inputs for the settings screen: the version load outcome (with the
 * consent flag read alongside), the identity of the student, the async uri resolutions that
 * re-enter as effect-carrying symbols, the assistant observations and write outcome, and the
 * outcome of deleting the assistant conversations.
 */
sealed interface SettingsInternalEvent {
	data class SettingsVersionLoaded(
		val versionText: String,
		val usageDataCollectionEnabled: Boolean
	) : SettingsInternalEvent

	data class SettingsVersionLoadFailed(
		val versionFallbackText: String,
		val usageDataCollectionEnabled: Boolean
	) : SettingsInternalEvent

	data class ProfileIdentityObserved(
		val identity: ProfileIdentity?
	) : SettingsInternalEvent

	data class SupportEmailUriLoaded(
		val uri: String
	) : SettingsInternalEvent

	data class StoreUriLoaded(
		val uri: String
	) : SettingsInternalEvent

	data class AssistantAvailabilityObserved(
		val visible: Boolean
	) : SettingsInternalEvent

	data class AssistantConsentObserved(
		val granted: Boolean
	) : SettingsInternalEvent

	data class AssistantConsentWriteFailed(
		val message: String
	) : SettingsInternalEvent

	data class AssistantConversationsDeleted(
		val message: String
	) : SettingsInternalEvent

	data class AssistantConversationsDeleteFailed(
		val message: String
	) : SettingsInternalEvent
}
