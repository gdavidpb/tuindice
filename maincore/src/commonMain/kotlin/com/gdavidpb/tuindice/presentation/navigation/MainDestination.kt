package com.gdavidpb.tuindice.presentation.navigation

import com.gdavidpb.tuindice.base.presentation.navigation.Destination
import com.gdavidpb.tuindice.base.presentation.navigation.DialogDestination
import kotlinx.serialization.Serializable

@Serializable
sealed class MainDestination : Destination() {
	@Serializable
	data object GooglePlayServicesUnavailableDialog : MainDestination(), DialogDestination
}