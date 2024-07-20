package com.gdavidpb.tuindice.presentation.navigation

import com.gdavidpb.tuindice.base.presentation.navigation.Destination2
import kotlinx.serialization.Serializable

@Serializable
sealed class MainDestination : Destination2() {
	@Serializable
	data object GooglePlayServicesUnavailableDialog : MainDestination()
}