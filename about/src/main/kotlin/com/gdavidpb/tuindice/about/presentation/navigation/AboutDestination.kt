package com.gdavidpb.tuindice.about.presentation.navigation

import com.gdavidpb.tuindice.base.presentation.navigation.Destination2
import kotlinx.serialization.Serializable

@Serializable
sealed class AboutDestination : Destination2() {
	@Serializable
	data object NavGraph : AboutDestination()

	@Serializable
	data object About : AboutDestination()
}