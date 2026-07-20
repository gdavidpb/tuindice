package com.gdavidpb.tuindice.summary.presentation.navigation

import com.gdavidpb.tuindice.base.presentation.navigation.NavResult
import kotlinx.serialization.Serializable

@Serializable
sealed class ProfilePictureSettingsResult : NavResult {
	@Serializable
	data object Pick : ProfilePictureSettingsResult()

	@Serializable
	data object Take : ProfilePictureSettingsResult()

	@Serializable
	data object Remove : ProfilePictureSettingsResult()
}
