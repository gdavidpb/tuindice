package com.gdavidpb.tuindice.summary.presentation.navigation

import com.gdavidpb.tuindice.base.presentation.navigation.Destination
import com.gdavidpb.tuindice.base.presentation.navigation.DialogDestination
import kotlinx.serialization.Serializable

@Serializable
sealed class SummaryDestination : Destination() {
	@Serializable
	data object Summary : SummaryDestination()

	@Serializable
	data class ProfilePictureSettingsDialog(
		val showRemove: Boolean,
		val isCameraAvailable: Boolean
	) : SummaryDestination(), DialogDestination

	@Serializable
	data object RemoveProfilePictureConfirmationDialog : SummaryDestination(), DialogDestination

	// The Statistics screen, pushed from the hub: read-only, over the tab it is opened from.
	@Serializable
	data object Statistics : SummaryDestination()

	// The term report ("Tu trimestre") of the insight [insightId], pushed over the summary: read-only but for its thumbs.
	@Serializable
	data class TermReport(
		val insightId: String
	) : SummaryDestination()

	// The sheet that shares a closed term as an image, over the screen it is opened from. [termKey] is the term it opens
	// on, or null for the newest one.
	@Serializable
	data class ShareTermDialog(
		val termKey: String?
	) : SummaryDestination(), DialogDestination
}
