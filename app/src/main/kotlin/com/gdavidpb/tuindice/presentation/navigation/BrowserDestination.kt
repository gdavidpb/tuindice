package com.gdavidpb.tuindice.presentation.navigation

import com.gdavidpb.tuindice.base.presentation.navigation.Destination
import kotlinx.serialization.Serializable

@Serializable
sealed class BrowserDestination : Destination() {
	@Serializable
	data class Browser(
		val url: String
	) : Destination()

	@Serializable
	data class ExternalResourceDialog(
		val url: String
	) : BrowserDestination()
}