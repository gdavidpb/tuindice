package com.gdavidpb.tuindice.presentation.machine

import com.gdavidpb.tuindice.base.domain.model.UpdateAction
import com.gdavidpb.tuindice.base.presentation.navigation.Destination

/**
 * Internal machine inputs for the app host: the startup lifecycle split per outcome,
 * plus the platform flow triggers (review, update) and the wizard start approval.
 */
sealed interface MainInternalEvent {
	data object StartUpStarting : MainInternalEvent

	data class StartUpCompleted(
		val startDestination: Destination
	) : MainInternalEvent

	data class StartUpFailed(
		val noServices: Boolean
	) : MainInternalEvent

	data object ReviewRequested : MainInternalEvent

	data class UpdateInfoLoaded(
		val action: UpdateAction
	) : MainInternalEvent

	data object WizardStartApproved : MainInternalEvent
}
