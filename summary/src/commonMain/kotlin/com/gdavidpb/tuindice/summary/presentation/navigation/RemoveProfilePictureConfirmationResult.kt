package com.gdavidpb.tuindice.summary.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class RemoveProfilePictureConfirmationResult {
	@Serializable
	data object Confirmed : RemoveProfilePictureConfirmationResult()
}
