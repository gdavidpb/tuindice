package com.gdavidpb.tuindice.pensum.presentation.navigation

import com.gdavidpb.tuindice.base.presentation.navigation.Destination
import kotlinx.serialization.Serializable

@Serializable
sealed class PensumDestination : Destination() {
	@Serializable
	data object NavGraph : PensumDestination()

	@Serializable
	data object Pensum : PensumDestination()
}
