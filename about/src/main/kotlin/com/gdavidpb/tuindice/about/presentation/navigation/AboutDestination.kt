package com.gdavidpb.tuindice.about.presentation.navigation

import com.gdavidpb.tuindice.base.presentation.navigation.Destination
import kotlinx.serialization.Serializable

@Serializable
sealed class AboutDestination : Destination() {
	@Serializable
	data object NavGraph : AboutDestination()

	@Serializable
	data object About : AboutDestination()
}