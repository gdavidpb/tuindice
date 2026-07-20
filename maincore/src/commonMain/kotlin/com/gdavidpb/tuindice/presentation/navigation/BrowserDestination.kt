package com.gdavidpb.tuindice.presentation.navigation

import com.gdavidpb.tuindice.base.presentation.navigation.Destination
import com.gdavidpb.tuindice.base.presentation.navigation.DialogDestination
import kotlinx.serialization.Serializable

@Serializable
sealed class BrowserDestination : Destination() {
	@Serializable
	data class Browser(
		val title: String,
		val url: String
	) : Destination()

	@Serializable
	data class ExternalResourceDialog(
		val url: String
	) : BrowserDestination(), DialogDestination
}