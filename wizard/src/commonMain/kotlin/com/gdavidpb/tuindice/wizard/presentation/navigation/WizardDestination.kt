package com.gdavidpb.tuindice.wizard.presentation.navigation

import com.gdavidpb.tuindice.base.presentation.navigation.Destination
import kotlinx.serialization.Serializable

@Serializable
sealed class WizardDestination : Destination() {
	@Serializable
	data object NavGraph : WizardDestination()

	@Serializable
	data object Wizard : WizardDestination()
}
