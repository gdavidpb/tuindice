package com.gdavidpb.tuindice.presentation.navigation

import com.gdavidpb.tuindice.base.presentation.navigation.Destination2
import kotlinx.serialization.Serializable

@Serializable
sealed class BrowserDestination : Destination2() {
	@Serializable
	data class Browser(
		val url: String
	) : Destination2()

	@Serializable
	data class ExternalResourceDialog(
		val url: String
	) : BrowserDestination()
}