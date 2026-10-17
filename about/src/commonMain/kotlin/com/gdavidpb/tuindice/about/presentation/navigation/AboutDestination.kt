package com.gdavidpb.tuindice.about.presentation.navigation

import com.gdavidpb.tuindice.base.presentation.navigation.Destination
import com.gdavidpb.tuindice.base.presentation.navigation.DialogDestination
import kotlinx.serialization.Serializable

@Serializable
sealed class AboutDestination : Destination() {
	@Serializable
	data object About : AboutDestination()

	// Asks before deleting every conversation with the assistant.
	@Serializable
	data object DeleteAssistantConversationsDialog : AboutDestination(), DialogDestination
}
