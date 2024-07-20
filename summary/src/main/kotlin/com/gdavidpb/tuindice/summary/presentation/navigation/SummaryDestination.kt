package com.gdavidpb.tuindice.summary.presentation.navigation

import com.gdavidpb.tuindice.base.presentation.navigation.Destination
import kotlinx.serialization.Serializable

@Serializable
sealed class SummaryDestination : Destination() {
	@Serializable
	data object NavGraph : SummaryDestination()

	@Serializable
	data object Summary : SummaryDestination()

	@Serializable
	data class ProfilePictureSettingsDialog(
		val showRemove: Boolean
	) : SummaryDestination()

	data object RemoveProfilePictureConfirmationDialog : SummaryDestination()
}