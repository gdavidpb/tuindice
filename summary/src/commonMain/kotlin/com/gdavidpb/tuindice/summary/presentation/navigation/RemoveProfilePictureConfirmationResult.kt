package com.gdavidpb.tuindice.summary.presentation.navigation

import com.gdavidpb.tuindice.base.presentation.navigation.NavResult
import kotlinx.serialization.Serializable

@Serializable
sealed class RemoveProfilePictureConfirmationResult : NavResult {
	@Serializable
	data object Confirmed : RemoveProfilePictureConfirmationResult()
}
