package com.gdavidpb.tuindice.summary.presentation.machine

import com.gdavidpb.tuindice.summary.presentation.contract.Summary

/**
 * Internal machine inputs: the user observation, the refresh lifecycle, and the two
 * profile-picture flows. Messages arrive pre-resolved (the jobs own resource loading).
 */
sealed interface SummaryInternalEvent {
	data class UserObserved(
		val content: Summary.State.Content
	) : SummaryInternalEvent

	data class ObservationFailed(
		val message: String
	) : SummaryInternalEvent

	data object RefreshSucceeded : SummaryInternalEvent

	data class RefreshFailed(
		val message: String
	) : SummaryInternalEvent

	data class ProfilePictureUploadStarted(
		val previewPath: String
	) : SummaryInternalEvent

	data class ProfilePictureUploadSucceeded(
		val message: String
	) : SummaryInternalEvent

	data class ProfilePictureUploadFailed(
		val message: String
	) : SummaryInternalEvent

	data object ProfilePictureRemovalStarted : SummaryInternalEvent

	data class ProfilePictureRemovalSucceeded(
		val message: String
	) : SummaryInternalEvent

	data class ProfilePictureRemovalFailed(
		val message: String,
		val clearProfilePicture: Boolean
	) : SummaryInternalEvent
}
