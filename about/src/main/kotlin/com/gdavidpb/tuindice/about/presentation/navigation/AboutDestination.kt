package com.gdavidpb.tuindice.about.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class AboutDestination {
	@Serializable
	data object NavGraph : AboutDestination()

	@Serializable
	data object About : AboutDestination()
}