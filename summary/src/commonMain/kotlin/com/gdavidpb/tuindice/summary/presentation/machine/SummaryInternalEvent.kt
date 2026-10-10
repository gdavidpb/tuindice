package com.gdavidpb.tuindice.summary.presentation.machine

import com.gdavidpb.tuindice.summary.presentation.contract.Summary
import com.gdavidpb.tuindice.summary.presentation.model.SummarySyncItem

/**
 * Internal machine inputs: the user and sync observations, the refresh lifecycle, what the
 * device can do for the picture options, and the two profile-picture flows. Messages arrive
 * pre-resolved (the jobs own resource loading).
 */
sealed interface SummaryInternalEvent {
	data class UserObserved(
		val content: Summary.State.Content
	) : SummaryInternalEvent

	data class ObservationFailed(
		val message: String
	) : SummaryInternalEvent

	/** The sync changed what it says of the account, or started or stopped running. */
	data class SyncObserved(
		val sync: SummarySyncItem
	) : SummaryInternalEvent

	/** The device answered whether it can take a picture, asked as the picture options open. */
	data class CameraAvailabilityResolved(
		val isCameraAvailable: Boolean
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
