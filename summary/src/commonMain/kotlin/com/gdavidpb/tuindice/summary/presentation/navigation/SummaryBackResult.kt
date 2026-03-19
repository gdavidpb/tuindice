package com.gdavidpb.tuindice.summary.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class ProfilePictureSettingsResult {
	@Serializable
	data object Pick : ProfilePictureSettingsResult()

	@Serializable
	data object Take : ProfilePictureSettingsResult()

	@Serializable
	data object Remove : ProfilePictureSettingsResult()
}

@Serializable
sealed class RemoveProfilePictureConfirmationResult {
	@Serializable
	data object Confirmed : RemoveProfilePictureConfirmationResult()
}
