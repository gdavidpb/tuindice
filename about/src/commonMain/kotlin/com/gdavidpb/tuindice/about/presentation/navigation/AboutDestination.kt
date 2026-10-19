package com.gdavidpb.tuindice.about.presentation.navigation

import com.gdavidpb.tuindice.base.presentation.navigation.Destination
import com.gdavidpb.tuindice.base.presentation.navigation.DialogDestination
import kotlinx.serialization.Serializable

@Serializable
sealed class AboutDestination : Destination() {
	// Settings, pushed over the tab on screen from the avatar in the top bar.
	@Serializable
	data object Settings : AboutDestination()

	// «Acerca de TuIndice», pushed from Settings: the version, the license, the developer and the credits.
	@Serializable
	data object About : AboutDestination()

	// Asks before deleting every conversation with the assistant.
	@Serializable
	data object DeleteAssistantConversationsDialog : AboutDestination(), DialogDestination
}
