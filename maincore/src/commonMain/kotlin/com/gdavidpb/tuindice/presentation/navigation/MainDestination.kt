package com.gdavidpb.tuindice.presentation.navigation

import com.gdavidpb.tuindice.base.presentation.navigation.Destination
import kotlinx.serialization.Serializable

@Serializable
sealed class MainDestination : Destination() {
	@Serializable
	data object GooglePlayServicesUnavailableDialog : MainDestination()
}