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
		val showRemove: Boolean
	) : SummaryDestination(), DialogDestination

	@Serializable
	data object RemoveProfilePictureConfirmationDialog : SummaryDestination(), DialogDestination
}
